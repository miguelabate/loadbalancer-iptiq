package com.iptiq.provider;


/**
 * Provider implementation that slows the response time to test the limit on parallel requests
 */
public class DelayedRequestProvider extends ProviderBasic{

    public DelayedRequestProvider(String identifier) {
        super(identifier);
    }
    @Override
    public String get() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //log. don't care it' s just for test
            throw new RuntimeException(e);
        }
        return super.get();
    }
    @Override
    public Boolean check() {
        return true;
    }
}
