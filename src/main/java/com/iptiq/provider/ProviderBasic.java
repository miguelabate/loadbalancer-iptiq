package com.iptiq.provider;

public class ProviderBasic implements Provider<String>{

    private String identifier;

    public ProviderBasic(String identifier) {
        this.identifier = identifier;
    }

    /**
     * In this case the data retrieves is the actual id os the provider
     */
    @Override
    public String get() {
        return this.identifier;
    }

    @Override
    public String getId() {
        return this.identifier;
    }
}
