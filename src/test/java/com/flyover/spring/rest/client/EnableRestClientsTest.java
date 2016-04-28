/**
 * 
 */
package com.flyover.spring.rest.client;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flyover.spring.rest.client.EnableRestClients;
import com.flyover.spring.rest.client.Endpoint;
import com.flyover.spring.rest.client.EndpointProvider;

/**
 * @author mramach
 *
 */
public class EnableRestClientsTest {
    
    @Test
    public void testProxyCreatedForTestConsulClient() {
        
        AnnotationConfigApplicationContext ctx = 
                new AnnotationConfigApplicationContext(TestConfiugration.class);
        
        try {

            assertEquals("Checking that a proxy was created for the test interface.", 
                    1, ctx.getBeanNamesForType(TestRestClient.class).length);
            
        } finally {
            
            ctx.close();
        }
        
    }
    
    @Test
    public void testProxyCreatedForTestConsulClient_FromPackage() {
        
        AnnotationConfigApplicationContext ctx = 
                new AnnotationConfigApplicationContext(TestConfiugrationFromPackage.class);
        
        try {

            assertEquals("Checking that a proxy was created for the test interface.", 
                    1, ctx.getBeanNamesForType(TestRestClient.class).length);
            
        } finally {
            
            ctx.close();
        }
        
    }
    
    @Configuration
    @EnableRestClients
    public static class TestConfiugration {
        
        @Bean
        public EndpointProvider endpointProvider() {
            return new EndpointProvider() {
                public List<Endpoint> resolve(String serviceName) {
                    return Arrays.asList(new Endpoint("localhost", 5678, "http"));
                }
            };
        }
        
    }
    
    @Configuration
    @EnableRestClients("com.flyover.spring.rest.client")
    public static class TestConfiugrationFromPackage {
        
        @Bean
        public EndpointProvider endpointProvider() {
            return new EndpointProvider() {
                public List<Endpoint> resolve(String serviceName) {
                    return Arrays.asList(new Endpoint("localhost", 5678, "http"));
                }
            };
        }
        
    }

}
