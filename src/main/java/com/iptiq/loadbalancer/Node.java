package com.iptiq.loadbalancer;

import com.iptiq.provider.Provider;
import com.iptiq.provider.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A node represents a provider with some status data about it.
 * @param <T>
 */
@Getter@Setter
@AllArgsConstructor
public class Node<T> {
    private Provider<T> provider;
    private ProviderStatus status;
    private AtomicInteger healthCount;
    private AtomicInteger activeCallsCount;
}
