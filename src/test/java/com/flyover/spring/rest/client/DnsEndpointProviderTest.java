/**
 * 
 */
package com.flyover.spring.rest.client;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.flyover.spring.rest.client.DnsEndpointProvider;
import com.flyover.spring.rest.client.Endpoint;
import com.spotify.dns.DnsSrvResolver;
import com.spotify.dns.LookupResult;

/**
 * @author mramach
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DnsEndpointProviderTest {

    @Mock
    private DnsSrvResolver resolver;
    
    @Test
    public void testResolveWithDefaultScheme() {
        
        DnsEndpointProvider provider = new DnsEndpointProvider();
        provider.setResolver(resolver);
        
        when(resolver.resolve(isA(String.class))).thenReturn(
                Arrays.asList(LookupResult.create("10.10.10.2", 8080, 1, 30, 0)));
        
        List<Endpoint> endpoints = provider.resolve("service.domain.io");
        
        assertNotNull(endpoints);
        assertEquals(1, endpoints.size());
        
        Endpoint ep = endpoints.get(0);
        
        assertEquals("10.10.10.2", ep.getAddress());
        assertEquals(new Integer(8080), ep.getPort());
        assertEquals("http", ep.getScheme());
        
    }
    
    @Test
    public void testResolveWithSsl() {
        
        DnsEndpointProvider provider = new DnsEndpointProvider();
        provider.setResolver(resolver);
        
        when(resolver.resolve(isA(String.class))).thenReturn(
                Arrays.asList(LookupResult.create("10.10.10.2", 443, 1, 30, 0)));
        
        List<Endpoint> endpoints = provider.resolve("_ssl.service.domain.io");
        
        assertNotNull(endpoints);
        assertEquals(1, endpoints.size());
        
        Endpoint ep = endpoints.get(0);
        
        assertEquals("10.10.10.2", ep.getAddress());
        assertEquals(new Integer(443), ep.getPort());
        assertEquals("https", ep.getScheme());
        
    }
    
}
