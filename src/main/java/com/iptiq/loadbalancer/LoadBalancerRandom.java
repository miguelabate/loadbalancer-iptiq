package com.iptiq.loadbalancer;

import com.iptiq.exception.ErrorCallingProviderInstance;
import com.iptiq.exception.NoRegisteredProvidersInLoadBalancer;
import com.iptiq.exception.UnableToRegisterProviderInstance;
import com.iptiq.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class LoadBalancerRandom<T> implements LoadBalancer<T>{

    private List<Provider<T>> registeredProviders;
    private final ReentrantLock lockRegisterProvider;
    private Integer maxNumberOfProviders = 10;
    private Random rand = new Random();

    public LoadBalancerRandom() {
        this.lockRegisterProvider = new ReentrantLock();
        this.registeredProviders = new ArrayList<>();
    }

    @Override
    public T get() {
        lockRegisterProvider.lock();//lock on the providers
        try {
            Integer upperbound = this.registeredProviders.size();
            if(upperbound==0){
                throw new NoRegisteredProvidersInLoadBalancer("There are no providers registered in the load balancer");
            }
            T responseValue = this.registeredProviders.get(this.rand.nextInt(upperbound)).get();
            return responseValue;
        }catch (NoRegisteredProvidersInLoadBalancer e){//just re throw
            throw e;
        }catch (Exception e){//in case of any error making the call, throw exception
            throw new ErrorCallingProviderInstance("There was an error calling the provider");
        }finally {
            lockRegisterProvider.unlock();
        }
    }

    @Override
    public Integer registerProviderInstance(Provider<T> aProvider) throws UnableToRegisterProviderInstance {
        lockRegisterProvider.lock();
        try {
            if (this.registeredProviders.size() == maxNumberOfProviders) {
                throw new UnableToRegisterProviderInstance("Max number of providers registered reached");
            }
            this.registeredProviders.add(aProvider);
            return registeredProviders.size();
        }finally {
            lockRegisterProvider.unlock();
        }
    }

    @Override
    public Integer getProviderCount(){
        lockRegisterProvider.lock();
        try {
            return this.registeredProviders.size();
        }finally {
            lockRegisterProvider.unlock();
        }
    }

}
