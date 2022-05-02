package com.iptiq.loadbalancer;

import com.iptiq.provider.Provider;
import com.iptiq.provider.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A node represents a provider with some status data about it.
 * The status can be OK (the node is healthy and included in the loadbalancer logic),
 * EXCLUDED (the node was manually excluded so ignored from the loadbalancer logic) or UNHEALTHY (excluded because the check failed, needs to pass 2 healthchecks to be back)
 *
 * The healthCount is used to count the number of healthy checks before it goes back to OK
 *
 * activeCallsCount is used to count the active parallel calls. Th enode rejects new calls if the max of parallel calls is reached
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
