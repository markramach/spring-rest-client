/**
 * 
 */
package com.flyover.spring.rest.client;


/**
 * @author mramach
 *
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {
    
    private EndpointProvider endpointProvider;
    private RestClient annotation;
    private String serviceName;
    
    public void setAnnotation(RestClient annotation) {
        this.annotation = annotation;
    }

    public RestClient getAnnotation() {
        return annotation;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
    
    public EndpointProvider getEndpointProvider() {
        return endpointProvider;
    }

    public void setEndpointProvider(EndpointProvider endpointProvider) {
        this.endpointProvider = endpointProvider;
    }

}
