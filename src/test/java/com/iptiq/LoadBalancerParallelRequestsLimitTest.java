package com.iptiq;

import com.iptiq.exception.NoNodesAvailableException;
import com.iptiq.loadbalancer.LoadBalancerRoundRobinEnhanced;
import com.iptiq.provider.DelayedRequestProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadBalancerParallelRequestsLimitTest {

    @Test
    public void testGetValueFromProvider_ParallelLimitRequestHit() throws InterruptedException {
        List<Thread> myThreads = new ArrayList<>();//keep a list of threads to wait on
        AtomicInteger exceptionCount = new AtomicInteger(0);//to count the number of exception from the threads
        AtomicInteger correctResponsesCount = new AtomicInteger(0);//to count the number of correct responses from the threads

        //given: the load balancer with a heartbeat time period of 500 millis
        LoadBalancerRoundRobinEnhanced loadBalancer = new LoadBalancerRoundRobinEnhanced(500);

        //when: register 5 providers (that will delay their response)
        for (int i = 0; i < 5; i++) {
            loadBalancer.registerProviderInstance(new DelayedRequestProvider("id" + i));
        }
        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //then: no exceptions and the count is correct
        assertTrue(loadBalancer.getProviderCount() == 5);

        //then: the first 50 request should go fine, 5 give exception because teh cluster it's at max capacity
        for(int i=0;i<55;i++) {
            int finalI = i;
            Thread aThread = (new Thread(() -> {
                try {
                    loadBalancer.get();
                    correctResponsesCount.addAndGet(1);
                }catch (NoNodesAvailableException e){
                    exceptionCount.addAndGet(1);
                }
            }));
            myThreads.add(aThread);
            aThread.start();
        }
        for (Thread thread : myThreads) {
            thread.join();
        }

        assertEquals(5, exceptionCount.get());
        assertEquals(50, correctResponsesCount.get());
    }

}
