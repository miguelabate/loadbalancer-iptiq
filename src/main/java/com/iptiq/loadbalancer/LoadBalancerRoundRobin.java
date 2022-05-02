package com.iptiq.loadbalancer;

import com.iptiq.exception.ErrorCallingProviderInstance;
import com.iptiq.exception.NoRegisteredProvidersInLoadBalancer;
import com.iptiq.exception.UnableToRegisterProviderInstance;
import com.iptiq.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class LoadBalancerRoundRobin<T> implements LoadBalancer<T>{

    private List<Provider<T>> registeredProviders;
    private final ReentrantLock lockRegisterProvider;
    private Integer maxNumberOfProviders = 10;
    private Integer nextIndexToUse = 0;

    public LoadBalancerRoundRobin() {
        this.lockRegisterProvider = new ReentrantLock();
        this.registeredProviders = new ArrayList<>();
    }

    @Override
    public T get() {
        lockRegisterProvider.lock();//lock on the providers
        try {
            if(this.registeredProviders.size()==0){
                throw new NoRegisteredProvidersInLoadBalancer("There are no providers registered in the load balancer");
            }
            Provider<T> selectedProvider = this.registeredProviders.get(nextIndexToUse);
            nextIndexToUse=(nextIndexToUse+1)%this.registeredProviders.size();//increment index or go back if reached end
            lockRegisterProvider.unlock(); //can unlock now while calling the provider

            return selectedProvider.get();
        }catch (NoRegisteredProvidersInLoadBalancer e){//this one just re throw
            throw e;
        }catch (Exception e){//in case of any error making the call, throw exception
            throw new ErrorCallingProviderInstance("There was an error calling the provider");
        }finally {
            if(lockRegisterProvider.isLocked())
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
