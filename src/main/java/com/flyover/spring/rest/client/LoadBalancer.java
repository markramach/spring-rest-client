/**
 * 
 */
package com.flyover.spring.rest.client;

import java.lang.reflect.Method;

/**
 * @author mramach
 *
 */
public interface LoadBalancer {

    <T> T execute(Method method, Object...args);

    void setAnnotation(RestClient annotation);

    void setServiceName(String serviceName);

    EndpointProvider getEndpointProvider();

    void setEndpointProvider(EndpointProvider endpointProvider);

    String getServiceName();
    
}
