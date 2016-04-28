/**
 * 
 */
package com.flyover.spring.rest.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.flyover.spring.rest.client.EnableRestClients;
import com.flyover.spring.rest.client.Endpoint;
import com.flyover.spring.rest.client.EndpointProvider;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author mramach
 *
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class RestClientFactoryBeanTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(5678);
    
    @Autowired
    private TestRestClient client;
    
    @Test
    public void testCreateInstance() {
        
        stubFor(get(urlMatching("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"hello\":\"world\"}")));
        
        assertNotNull("Checking that the repsonse is not null.", client.get());
        
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
    
}
