package com.iptiq;

import com.iptiq.exception.NoNodesAvailableException;
import com.iptiq.loadbalancer.LoadBalancerRoundRobinEnhanced;
import com.iptiq.provider.FailHeartBeatProvider;
import com.iptiq.provider.ProviderBasic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadBalancerHeartBeatCheckTest {

    @Test
    public void testGetValueFromProvider_HeartBeatCheckHalfFail() throws InterruptedException {
        //given: the load balancer with a heartbeat time period of 1 sec
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>(500);

        //when: register 10 providers, 5 are unhealthy
        for (int i = 0; i < 5; i++) {
            loadBalancer.registerProviderInstance(new ProviderBasic("id" + i));
        }
        for (int i = 5; i < 10; i++) {
            loadBalancer.registerProviderInstance(new FailHeartBeatProvider("id" + i));
        }
        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //then: no exceptions and the count is correct, the register Provider locking mechanisms works fine
        assertTrue(loadBalancer.getProviderCount() == 10);

        //then: provider with id3 is excluded from the round robin
        String value = loadBalancer.get();
        assertEquals(value, "id0");
        value = loadBalancer.get();
        assertEquals(value, "id1");
        value = loadBalancer.get();
        assertEquals(value, "id2");
        value = loadBalancer.get();
        assertEquals(value, "id3");
        value = loadBalancer.get();
        assertEquals(value, "id4");
        value = loadBalancer.get();
        assertEquals(value, "id0");
        value = loadBalancer.get();
        assertEquals(value, "id1");

        //shutdown teh loadbalancer
        loadBalancer.shutdown();
    }

    @Test(expected = NoNodesAvailableException.class)
    public void testGetValueFromProvider_AllHeartBeatFail_NoAvailable() throws InterruptedException {
        //given: the load balancer with a heartbeat time period of 1 sec
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>(500);

        //when: register 10 unhealthy providers
        for (int i = 0; i < 10; i++) {
            loadBalancer.registerProviderInstance(new FailHeartBeatProvider("id" + i));
        }

        //then: the providers are there
        assertTrue(loadBalancer.getProviderCount() == 10);
        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //then: it will fail because none of the providers are healthy, none viable
        try {
            loadBalancer.get();
        }catch (NoNodesAvailableException e){
            assertEquals("There are no available provider nodes to resolve the get operation",e.getErrorMessage());
            throw e;
        }
    }
}
