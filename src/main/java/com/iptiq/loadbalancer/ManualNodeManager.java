package com.iptiq.loadbalancer;

/**
 * Interface to enable node inclusion/exclusion from a load balancer
 */
public interface ManualNodeManager {
    void includeProviderInstance(String providerId);
    void excludeProviderInstance(String providerId);
}
