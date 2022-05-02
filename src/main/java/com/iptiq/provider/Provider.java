package com.iptiq.provider;

/**
 * Interface representing a provider behind the loadbalancer
 * @param <T>
 */
public interface Provider<T> {

    /**
     * Gets the data that this provider retrieves
     * @return
     */
    T get();

    /**
     * Does a health check of the provider. True: healthy, false: unhealthy
     * @return
     */
    default Boolean check() {
        return true;
    }

    /**
     * Retrives the id of this provider
     * @return
     */
    String getId();
}
