/**
 * 
 */
package com.flyover.spring.rest.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.util.ClassUtils;

import com.flyover.spring.rest.client.DynamicRestClient;
import com.flyover.spring.rest.client.Endpoint;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author mramach
 *
 */
public class DynamicRestClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(5678);
    
    private Endpoint endpoint = new Endpoint("localhost", 5678, "http");
    
    @Test
    public void testExecute() {
     
        stubFor(get(urlMatching("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"hello\":\"world\"}")));
        
        DynamicRestClient client = new DynamicRestClient(Collections.emptyList());
        
        Method m = ClassUtils.getMethod(TestRestClient.class, "get");
        
        Map<String, String> result = client.execute(endpoint, m);
        
        assertNotNull("Checking that the client operation was successful.", result);
        assertEquals("Checing that the response was deserialized properly", "world", result.get("hello"));
        
        verify(getRequestedFor(urlMatching("/test")));
        
    }
    
    @Test
    public void testExecute_WithPathParameter() {
     
        stubFor(get(urlMatching("/test/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"hello\":\"world\"}")));
        
        DynamicRestClient client = new DynamicRestClient(Collections.emptyList());
        
        Method m = ClassUtils.getMethod(TestRestClient.class, "get", String.class);
        
        Map<String, String> result = client.execute(endpoint, m, "1234");
        
        assertNotNull("Checking that the client operation was successful.", result);
        assertEquals("Checing that the response was deserialized properly", "world", result.get("hello"));
        
        verify(getRequestedFor(urlMatching("/test/1234")));
        
    }
    
    @Test
    public void testExecute_WithRequestHeader() {
     
        stubFor(get(urlMatching("/test/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"hello\":\"world\"}")));
        
        DynamicRestClient client = new DynamicRestClient(Collections.emptyList());
        
        Method m = ClassUtils.getMethod(TestRestClient.class, "get", String.class, String.class);
        
        Map<String, String> result = client.execute(endpoint, m, "Token 1234", "1234");
        
        assertNotNull("Checking that the client operation was successful.", result);
        assertEquals("Checing that the response was deserialized properly", "world", result.get("hello"));
        
        verify(getRequestedFor(urlMatching("/test/1234"))
                .withHeader("Authorization", matching("Token 1234")));
        
    }
    
    @Test
    public void testExecute_WithQueryParameter() {
     
        stubFor(get(urlMatching("/test/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"hello\":\"world\"}")));
        
        DynamicRestClient client = new DynamicRestClient(Collections.emptyList());
        
        Method m = ClassUtils.getMethod(TestRestClient.class, "get", String.class, String.class, String.class);
        
        Map<String, String> result = client.execute(endpoint, m, "5678", "Token 1234", "1234");
        
        assertNotNull("Checking that the client operation was successful.", result);
        assertEquals("Checing that the response was deserialized properly", "world", result.get("hello"));
        
        verify(getRequestedFor(urlMatching("/test/1234.*"))
                .withQueryParam("param", matching("5678")));
        
    }
    
    @Test
    public void testExecute_WithRequestBody() {
     
        stubFor(post(urlMatching("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"hello\":\"world\"}")));
        
        DynamicRestClient client = new DynamicRestClient(Collections.emptyList());
        
        Method m = ClassUtils.getMethod(TestRestClient.class, "post", Map.class);
        
        Map<String, String> result = client.execute(endpoint, m, Collections.singletonMap("hello", "world"));
        
        assertNotNull("Checking that the client operation was successful.", result);
        assertEquals("Checing that the response was deserialized properly", "world", result.get("hello"));
        
        verify(postRequestedFor(urlMatching("/test")).withRequestBody(equalTo("{\"hello\":\"world\"}")));
        
    }
    
    @Test(expected = RuntimeException.class)
    public void testExecute_WithAmbiguousHttpMethod() {
     
        DynamicRestClient client = new DynamicRestClient(Collections.emptyList());
        
        Method m = ClassUtils.getMethod(TestRestClient.class, "ambiguous");
        
        client.execute(endpoint, m);
        
        fail("Methods with multiple HTTP Methods defined should thrown an exception indicating ambiguity.");
        
    }
    
}
