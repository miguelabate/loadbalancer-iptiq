package com.iptiq.loadbalancer;

import com.iptiq.exception.ErrorCallingProviderInstanceException;
import com.iptiq.exception.NoRegisteredProvidersInLoadBalancerException;
import com.iptiq.exception.UnableToRegisterProviderInstanceException;
import com.iptiq.provider.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
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
                throw new NoRegisteredProvidersInLoadBalancerException("There are no providers registered in the load balancer");
            }
            T responseValue = this.registeredProviders.get(this.rand.nextInt(upperbound)).get();
            return responseValue;
        }catch (NoRegisteredProvidersInLoadBalancerException e){//just re throw
            throw e;
        }catch (Exception e){//in case of any error making the call, throw exception
            throw new ErrorCallingProviderInstanceException("There was an error calling the provider");
        }finally {
            lockRegisterProvider.unlock();
        }
    }

    @Override
    public Integer registerProviderInstance(Provider<T> aProvider) throws UnableToRegisterProviderInstanceException {
        lockRegisterProvider.lock();
        try {
            if (this.registeredProviders.size() == maxNumberOfProviders) {
                throw new UnableToRegisterProviderInstanceException("Max number of providers registered reached");
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
