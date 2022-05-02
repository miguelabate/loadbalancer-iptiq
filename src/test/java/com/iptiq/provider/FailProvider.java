package com.iptiq.provider;


/**
 * new class from Provider that fails. Used for test.
 */
public  class FailProvider extends ProviderBasic{

    public FailProvider(String identifier) {
        super(identifier);
    }

    @Override
    public String get() {
        throw new RuntimeException();
    }
}
