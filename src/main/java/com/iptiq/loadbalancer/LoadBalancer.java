package com.iptiq.loadbalancer;

import com.iptiq.exception.UnableToRegisterProviderInstance;
import com.iptiq.provider.Provider;

public interface LoadBalancer<T> {

    /**
     * Dispatches the get operation to one provider and retrieves the result to the caller
     * @return
     */
    T get();

    /**
     * Registers a provider into this load balancer and returns the current count of registered providers
     * @param aProvider
     * @return
     * @throws UnableToRegisterProviderInstance
     */
    Integer registerProviderInstance(Provider<T> aProvider) throws UnableToRegisterProviderInstance;

    Integer getProviderCount();

    /**
     * Liberates any resources that might have been created/acquired by the loadbalancer.
     */
    default void shutdown() {
        //by default do nothing
    }
}
