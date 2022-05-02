package com.iptiq.provider;


/**
 * new class from Provider that fails. Used for test.
 */
public class FailHeartBeatProvider extends ProviderBasic{

    public FailHeartBeatProvider(String identifier) {
        super(identifier);
    }

    @Override
    public Boolean check() {
        return false;
    }
}
