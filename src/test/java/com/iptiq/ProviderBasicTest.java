package com.iptiq;

import com.iptiq.provider.ProviderBasic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProviderBasicTest {
    @Test
    public void testGetIdentifier()
    {
        //given: a basic provider
        ProviderBasic provider = new ProviderBasic("id1");

        //when: call get
        String value = provider.get();

        //then: the identifier is retrived
        assertEquals(value,"id1");
    }

}
