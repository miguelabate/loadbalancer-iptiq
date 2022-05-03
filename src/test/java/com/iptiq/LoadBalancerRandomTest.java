package com.iptiq;

import com.iptiq.exception.ErrorCallingProviderInstanceException;
import com.iptiq.exception.NoRegisteredProvidersInLoadBalancerException;
import com.iptiq.exception.UnableToRegisterProviderInstanceException;
import com.iptiq.loadbalancer.LoadBalancerRandom;
import com.iptiq.provider.FailProvider;
import com.iptiq.provider.ProviderBasic;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadBalancerRandomTest {

    @Test(expected = Test.None.class)
    public void testRegisterProviders_SingleThread_happyFlow()
    {
        Integer countLB = 0;
        //given: a loadbalancer
        LoadBalancerRandom<String> loadBalancer = new LoadBalancerRandom<String>();

        //when: call register provider 10 times
        for(int i=0;i<10;i++) {
            countLB = loadBalancer.registerProviderInstance(new ProviderBasic("id" + i));
        }

        //then: no exceptions
        assertEquals(10, (int) countLB);
    }

    @Test(expected = UnableToRegisterProviderInstanceException.class)
    public void testRegisterProviders_SingleThread_failAddingMoreThanAccepted()
    {
        //given: a load balancer
        LoadBalancerRandom<String> loadBalancer = new LoadBalancerRandom<String>();

        //when: call register provider 11 times
        for(int i=0;i<11;i++)
            loadBalancer.registerProviderInstance(new ProviderBasic("id"+i));

        //then: UnableToRegisterProviderInstance exception is thrown
    }

    @SneakyThrows
    @Test
    public void testRegisterProviders_MultiThread_happyFlow() {
        List<Thread> myThreads = new ArrayList<>();//keep a list of threads to wait on

        //given: a basic loadBalancer
        LoadBalancerRandom<String> loadBalancer = new LoadBalancerRandom<String>();

        //when: call register provider 10 times, with multiple thread
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

    @SneakyThrows
    @Test
    public void testRegisterProviders_MultiThread_failAddingMoreThanAccepted() {
        List<Thread> myThreads = new ArrayList<>();//keep a list of threads to wait on

        AtomicInteger exceptionCount = new AtomicInteger(0);//to count the number of exception from the threads

        //given: a basic loadBalancer
        LoadBalancerRandom<String> loadBalancer = new LoadBalancerRandom<String>();

        //when: call register provider 30 times, with multiple thread
        for(int i=0;i<30;i++) {
            int finalI = i;
            Thread aThread = (new Thread(() -> {
                try {
                    loadBalancer.registerProviderInstance(new ProviderBasic("id" + finalI));
                }catch (UnableToRegisterProviderInstanceException e){
                    exceptionCount.addAndGet(1);
                }
            }));
            myThreads.add(aThread);
            aThread.start();
        }
        for (Thread thread : myThreads) {
            thread.join();
        }

        //then: 10 providers where added while 20 couldn't be added |(exception thrown)
        assertTrue(loadBalancer.getProviderCount() == 10);
        assertTrue(exceptionCount.get() == 20);
    }


    @SneakyThrows
    @Test
    public void testGetValueFromProvider_happyFlow() {
        List<Thread> myThreads = new ArrayList<>();//keep a list of threads to wait on
        List<String> possibleIds = new ArrayList<>();

        //given: a basic loadBalancer
        LoadBalancerRandom<String> loadBalancer = new LoadBalancerRandom<>();

        //when: call register provider 10 times, with multiple thread
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
        assertTrue(loadBalancer.getProviderCount() == 10);

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
    public void testGetValueFromProvider_fail_NoProviders() {
        //given: a basic provider, empty
        LoadBalancerRandom<String> loadBalancer = new LoadBalancerRandom<>();

        //when-then: getting the value, we get exception because there are no providers
        try {
            loadBalancer.get();
        }catch (NoRegisteredProvidersInLoadBalancerException e){
            assertEquals("There are no providers registered in the load balancer", e.getErrorMessage());
        }
    }

    @Test
    public void testGetValueFromProvider_fail_ProviderCallFails() {
        //given: a loadBalancer with a provider registered that will fail teh get()
        LoadBalancerRandom<String> loadBalancer = new LoadBalancerRandom<>();

        loadBalancer.registerProviderInstance(new FailProvider("id0"));

        //when: getting the value, we get exception because there are no providers
        try {
            loadBalancer.get();
        }catch (ErrorCallingProviderInstanceException e){
            assertEquals("There was an error calling the provider", e.getErrorMessage());
        }
    }


}
