package com.iptiq;

import com.iptiq.exception.ErrorCallingProviderInstance;
import com.iptiq.exception.NoNodesAvailableException;
import com.iptiq.exception.NoRegisteredProvidersInLoadBalancer;
import com.iptiq.exception.UnableToRegisterProviderInstance;
import com.iptiq.loadbalancer.LoadBalancerRoundRobin;
import com.iptiq.loadbalancer.LoadBalancerRoundRobinEnhanced;
import com.iptiq.provider.FailProvider;
import com.iptiq.provider.ProviderBasic;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadBalancerRoundRobinEnhancedTest {

    @Test(expected = Test.None.class)
    public void testRegisterProviders_SingleThread_happyFlow()
    {
        Integer countLB = 0;
        //given: a basic provider
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>();

        //when: call register provider 10 times
        for(int i=0;i<10;i++) {
            countLB = loadBalancer.registerProviderInstance(new ProviderBasic("id" + i));
        }

        //then: no exceptions
        assertTrue(countLB == 10);
    }

    @Test(expected = UnableToRegisterProviderInstance.class)
    public void testRegisterProviders_SingleThread_failAddingMoreThanAccepted()
    {
        //given: a basic provider
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>();

        //when: call register provider 10 times
        for(int i=0;i<11;i++)
            loadBalancer.registerProviderInstance(new ProviderBasic("id"+i));

        //then:  exception is thrown
    }

    @Test
    public void testRegisterProviders_MultiThread_happyFlow() throws InterruptedException {
        List<Thread> myThreads = new ArrayList<>();//keep a lsit of threads to wait on

        //given: a basic provider
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>();

        //when: call register provider 9 times, with multiple thread
        for(int i=0;i<10;i++) {
            int finalI = i;
            Thread aThread = (new Thread(() -> loadBalancer.registerProviderInstance(new ProviderBasic("id" + finalI))));
            myThreads.add(aThread);
            aThread.start();
        }
        for (Thread thread : myThreads) {
            thread.join();
        }

        //then: no exceptions and the count is correct, the register Provider locking mechanisms works fine
        assertTrue(loadBalancer.getProviderCount() == 10);
    }

    @Test
    public void testRegisterProviders_MultiThread_failAddingMoreThanAccepted() throws InterruptedException {
        List<Thread> myThreads = new ArrayList<>();//keep a list of threads to wait on

        AtomicInteger exceptionCount = new AtomicInteger(0);//to count the number of exception from the threads

        //given: a basic provider
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>();

        //when: call register provider 30 times, with multiple thread
        for(int i=0;i<30;i++) {
            int finalI = i;
            Thread aThread = (new Thread(() -> {
                try {
                    loadBalancer.registerProviderInstance(new ProviderBasic("id" + finalI));
                }catch (UnableToRegisterProviderInstance e){
                    exceptionCount.addAndGet(1);
                }
            }));
            myThreads.add(aThread);
            aThread.start();
        }
        for (Thread thread : myThreads) {
            thread.join();
        }

        //then: 10 providers where added while 20 couldnt be added |(exception thrown)
        assertTrue(loadBalancer.getProviderCount() == 10);
        assertTrue(exceptionCount.get() == 20);
    }


    @Test
    public void testGetValueFromProvider_MultiThread_happyFlow() throws InterruptedException {
        List<Thread> myThreads = new ArrayList<>();//keep a lsit of threads to wait on
        List<String> possibleIds = new ArrayList<>();

        //given: a basic provider
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>(500);

        //when: call register provider 9 times, with multiple thread
        for(int i=0;i<10;i++) {
            int finalI = i;
            Thread aThread = (new Thread(() -> loadBalancer.registerProviderInstance(new ProviderBasic("id" + finalI))));
            myThreads.add(aThread);
            aThread.start();
            possibleIds.add("id" + finalI);//keeping the possible ids to check later int he resposnes
        }
        for (Thread thread : myThreads) {
            thread.join();
        }

        //then: no exceptions and the count is correct, the register Provider locking mechanisms works fine
        assertEquals(10, (int) loadBalancer.getProviderCount());
        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //when: getting the value, we get random provider id assigned
        String value = loadBalancer.get();
        assertTrue(possibleIds.contains(value));
        value = loadBalancer.get();
        assertTrue(possibleIds.contains(value));
        value = loadBalancer.get();
        assertTrue(possibleIds.contains(value));
        value = loadBalancer.get();
        assertTrue(possibleIds.contains(value));
    }

    @Test
    public void testGetValueFromProvider_SingleThread_happyFlow() throws InterruptedException {
        //given: a basic provider
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>(500);

        //when: call register provider 10 times, single thread
        for(int i=0;i<10;i++) {
             loadBalancer.registerProviderInstance(new ProviderBasic("id" + i));
        }

        //then: no exceptions and the count is correct, the register Provider locking mechanisms works fine
        assertTrue(loadBalancer.getProviderCount() == 10);

        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //when: getting the value, we get sequential ids in a round robin fashion
        String value;
        for(int i=0;i<20;i++) {
            value = loadBalancer.get();
            assertEquals(value, "id"+i%10);
        }
    }

    @Test
    public void testGetValueFromProvider_fail_NoProviders() {
        //given: a basic provider, empty
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>();

        //when: getting the value, we get exception because there are no providers
        try {
            loadBalancer.get();
        }catch (NoRegisteredProvidersInLoadBalancer e){
            assertEquals("There are no providers registered in the load balancer", e.getErrorMessage());
        }
    }

    @Test
    public void testGetValueFromProvider_fail_ProviderCallFails() throws InterruptedException {
        //given: a basic provider, empty
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>(500);

        loadBalancer.registerProviderInstance(new FailProvider("id0"));

        //when: getting the value, we get exception because there are no providers
        try {
            loadBalancer.get();
        }catch (NoNodesAvailableException e){
            assertEquals("There are no available provider nodes to resolve the get operation", e.getErrorMessage());
        }
    }

    @Test
    public void testGetValueFromProvider_ExcludeAndIncludeProvider() throws InterruptedException {
        //given: a basic provider
        LoadBalancerRoundRobinEnhanced<String> loadBalancer = new LoadBalancerRoundRobinEnhanced<>(500);

        //when: call register provider 10 times, single thread
        for(int i=0;i<10;i++) {
            loadBalancer.registerProviderInstance(new ProviderBasic("id" + i));
        }

        //then: no exceptions and the count is correct, the register Provider locking mechanisms works fine
        assertTrue(loadBalancer.getProviderCount() == 10);

        //when: exclude provider with id=id3
        loadBalancer.excludeProviderInstance("id3");

        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //then: provider with id3 is excluded from the round robin
        String value = loadBalancer.get();
        assertEquals(value, "id0");
        value = loadBalancer.get();
        assertEquals(value, "id1");
        value = loadBalancer.get();
        assertEquals(value, "id2");
        value = loadBalancer.get();
        assertEquals(value, "id4");

        //when: include the provider with id3 again
        loadBalancer.includeProviderInstance("id3");
        Thread.sleep(2000);//wait a bit for the heartbeat to go a round

        //then: now the provider with id3 is in the round robin
        value = loadBalancer.get();
        assertEquals(value, "id5");
        value = loadBalancer.get();
        assertEquals(value, "id6");
        value = loadBalancer.get();
        assertEquals(value, "id7");
        value = loadBalancer.get();
        assertEquals(value, "id8");
        value = loadBalancer.get();
        assertEquals(value, "id9");
        value = loadBalancer.get();
        assertEquals(value, "id0");
        value = loadBalancer.get();
        assertEquals(value, "id1");
        value = loadBalancer.get();
        assertEquals(value, "id2");
        value = loadBalancer.get();
        assertEquals(value, "id3");
    }

}
