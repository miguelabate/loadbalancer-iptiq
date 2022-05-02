package com.iptiq;

import com.iptiq.exception.NoNodesAvailableException;
import com.iptiq.loadbalancer.LoadBalancerRoundRobinEnhanced;
import com.iptiq.provider.ProviderBasic;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class ExampleLBRoundRobinEnhanced
{
    @SneakyThrows
    public static void main( String[] args )
    {
        //create a RoundRobin Enhanced Load balancer with a heart beat period of 500 milliseconds
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>(500);

        //register some providers
        for (int i = 0; i < 8; i++) {
            loadBalancer.registerProviderInstance(new ProviderBasic("id" + i));
        }

        //let' s exclude the node with id=id5
        loadBalancer.excludeProviderInstance("id5");

        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //do some get requests, in a serial way (same thread)
        System.out.println("### Single thread ###");
        for(int i=0;i<20;i++){
            System.out.println("Result from calling get() operation on LB: "+ loadBalancer.get());
        }

        //do some get requests, in parallel
        List<Thread> myThreads = new ArrayList<>();
        System.out.println("### Multiple thread ###");
        for(int i=0;i<20;i++) {
            Thread aThread = (new Thread(() -> {
                System.out.println("Thread id:" + Thread.currentThread().getId()+": Result from calling get() operation on LB: "+ loadBalancer.get());
                loadBalancer.get();
            }));
            aThread.start();
            myThreads.add(aThread);
        }
        //wait on all threads to finish before ending
        for (Thread thread : myThreads) {
            thread.join();
        }
        //shutdown teh loadbalancer
        loadBalancer.shutdown();
    }
}
