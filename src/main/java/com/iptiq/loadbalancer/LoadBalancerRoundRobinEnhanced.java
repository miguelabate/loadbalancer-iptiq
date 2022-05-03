package com.iptiq.loadbalancer;

import com.iptiq.exception.*;
import com.iptiq.provider.Provider;
import com.iptiq.provider.ProviderStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Load balancer that assigns providers in a round roobin way.
 * It has the capability of manually including/excluding nodes.
 * It has a heart beat running periodically on each node.
 * It has a limit on the parallel reqeust supported. (aliveProviders * providerMaxParallelReeusts)
 * @param <T>
 */
@Slf4j
public class LoadBalancerRoundRobinEnhanced<T> implements LoadBalancer<T>, ManualNodeManager {

    public static final int HEALTHY_THRESHOLD = 2; //the number of times a provider needs to respond as healthy before it becomes HEALTHY
    private List<Node<T>> registeredProviderNodes;
    private final ReentrantLock lockRegisterProvider;
    private Integer maxNumberOfProviders = 10;
    private Integer maxNumberParallelRequestsPerProvider = 10;
    private Integer nextIndexToUse = 0;

    private final Thread heartBeatCheckThread;

    public LoadBalancerRoundRobinEnhanced(Integer maxNumberParallelRequestsPerProvider, Integer healthCheckTimeMillis) {
        this(healthCheckTimeMillis);
        this.maxNumberParallelRequestsPerProvider = maxNumberParallelRequestsPerProvider;

    }
    public LoadBalancerRoundRobinEnhanced(Integer heartBeatTimeMillis) {
        this.lockRegisterProvider = new ReentrantLock();
        this.registeredProviderNodes = new ArrayList<>();
        this.heartBeatCheckThread = createHeartBeatThread(heartBeatTimeMillis);
        this.heartBeatCheckThread.start();
    }

    public LoadBalancerRoundRobinEnhanced() {
        this(1000);
    }
    @Override
    public T get() {
        //lock on the providers while doing the node selection
        lockRegisterProvider.lock();
        try {
            if(this.registeredProviderNodes.size()==0){
                log.warn("There are no providers registered in the load balancer");
                throw new NoRegisteredProvidersInLoadBalancerException("There are no providers registered in the load balancer");
            }
            //keep track of the number of visited nodes, fail in case we went a round without available nodes
            Integer visitedNodesCount=0;
            Integer numberOfExistingNodes = this.registeredProviderNodes.size();

            Node<T> selectedNode;

            //pick next available node while the current one is not usable (not OK or max parallel requests reached)
            do {
                selectedNode = this.registeredProviderNodes.get(nextIndexToUse);
                nextIndexToUse = (nextIndexToUse + 1) % this.registeredProviderNodes.size();//increment index or go back if reached end
                visitedNodesCount++;
                if(visitedNodesCount>numberOfExistingNodes){
                    log.warn("There are no available provider nodes to resolve the get operation. Registered nodes: {}",numberOfExistingNodes);
                    throw new NoNodesAvailableException("There are no available provider nodes to resolve the get operation");
                }
            }while (selectedNode.getStatus()!= ProviderStatus.OK || selectedNode.getActiveCallsCount().get()>=this.maxNumberParallelRequestsPerProvider);

            Provider<T> selectedProvider = selectedNode.getProvider();
            //can unlock now while making call to the provider
            lockRegisterProvider.unlock();

            selectedNode.getActiveCallsCount().addAndGet(1); //increase the active calls to the provider
            T responseValue;
            try {
                responseValue = selectedProvider.get();
            }catch(Exception e){
                log.error("There was an error from provider", e);
                throw new UpstreamServiceException("There was an error from upstream provider service");
            }finally {
                selectedNode.getActiveCallsCount().addAndGet(-1);//decrease the active calls to the provider, do this in here in case there was error
            }
            return responseValue;
        }catch (NoNodesAvailableException | NoRegisteredProvidersInLoadBalancerException e){//this one just re throw
            log.warn("No nodes available when trying to make request", e);
            throw e;
        } catch (Exception e){//in case of any error making the call, throw exception
            log.error("There was an error while calling the provider", e);
            throw new ErrorCallingProviderInstanceException("There was an error calling the provider");
        }finally {
            if(lockRegisterProvider.isLocked()&&lockRegisterProvider.isHeldByCurrentThread())
                lockRegisterProvider.unlock();
        }
    }

    @Override
    public Integer registerProviderInstance(Provider<T> aProvider) throws UnableToRegisterProviderInstanceException {
        lockRegisterProvider.lock();
        try {
            if (this.registeredProviderNodes.size() == maxNumberOfProviders) {
                throw new UnableToRegisterProviderInstanceException("Max number of providers registered reached");
            }
            Node<T> aNode = new Node<T>(aProvider,ProviderStatus.UNHEALTHY,new AtomicInteger(0),new AtomicInteger(0));

            this.registeredProviderNodes.add(aNode);
            return registeredProviderNodes.size();
        }finally {
            lockRegisterProvider.unlock();
        }
    }

    @Override
    public Integer getProviderCount(){
        lockRegisterProvider.lock();
        try {
            return this.registeredProviderNodes.size();
        }finally {
            lockRegisterProvider.unlock();
        }
    }

    @Override
    public void excludeProviderInstance(String providerId) throws ProviderNotFoundException {
        lockRegisterProvider.lock();
        try {
            Node<T> aNode = this.registeredProviderNodes.stream().filter(node -> node.getProvider().getId().compareTo(providerId)==0)
                    .findFirst().orElseThrow(() -> new ProviderNotFoundException("No provider found with id "+providerId));
            aNode.setStatus(ProviderStatus.EXCLUDED);
        }finally {
            lockRegisterProvider.unlock();
        }
    }

    @Override
    public void includeProviderInstance(String providerId) throws ProviderNotFoundException {
        lockRegisterProvider.lock();
        try {
            Node<T> aNode = this.registeredProviderNodes.stream().filter(node -> node.getProvider().getId().compareTo(providerId)==0)
                    .findFirst().orElseThrow(() -> new ProviderNotFoundException("No provider found with id "+providerId));
            aNode.setStatus(ProviderStatus.UNHEALTHY);//put it back as unhealthy and let the heart beat check to bring it up
        }finally {
            lockRegisterProvider.unlock();
        }
    }

    private Thread createHeartBeatThread(Integer healthCheckTimeMillis) {
        return new Thread(() -> {
            while (!Thread.interrupted()) {
                log.info("Running heart-beat for all nodes...");
                try {
                    lockRegisterProvider.lock();//lock on the providers
                    //pick all the nodes except the ones manually excluded to do the health-check
                    for (Node aNode : this.registeredProviderNodes.stream().filter(aNode -> aNode.getStatus() != ProviderStatus.EXCLUDED).collect(Collectors.toList())) {
                        Boolean isHealthy = aNode.getProvider().check();
                        if (!isHealthy) { //if heartbeat fails, mark as unhealthy and reset the healthcount
                            aNode.setStatus(ProviderStatus.UNHEALTHY);
                            aNode.getHealthCount().set(0);
                        } else {//if heartbeat is ok, increment the healthcount
                            Integer healthCount = aNode.getHealthCount().addAndGet(1);
                            if(healthCount>= HEALTHY_THRESHOLD) {
                                aNode.setStatus(ProviderStatus.OK);
                                aNode.getHealthCount().set(0);//reset the health counter
                            }
                        }
                    }
                    lockRegisterProvider.unlock();
                    //sleeps the configured period until next health check round
                    Thread.sleep(healthCheckTimeMillis);
                } catch (InterruptedException e) {
                    log.info("Destroying heart-beat thread...");
                    return;
                }
                catch (Exception e) {
                    log.error("There was a problem creating the health-check for the loadbalancer", e);
                    throw new LoadBalancerCreationException("There was a problem creating the health-check for the loadbalancer");
                }  finally {
                    if (lockRegisterProvider.isLocked()&&lockRegisterProvider.isHeldByCurrentThread())
                        lockRegisterProvider.unlock();
                }
            }

        });
    }

    @Override
    public void shutdown(){
        log.info("Shutting down load-balancer");
        //stop the heartbeat thread
        this.heartBeatCheckThread.interrupt();
    }
}
