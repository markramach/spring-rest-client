/**
 * 
 */
package com.flyover.spring.rest.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated interface is a dynamic Consul Client interface.
 * 
 * @author mramach
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestClient {
    
    /**
     * Defines the service name for the client. This will be 
     * used to determine available endpoints for client requests.
     * 
     * @return The service name for the client.
     */
    String value();
    
    /**
     * Indicates the load balancer implementation that should be 
     * used when making client requests.
     * 
     * @return The {@link LoadBalancer} type
     */
    Class<? extends LoadBalancer> loadBalancer() default FirstAvailableLoadBalancer.class;
    
    /**
     * If provided instructs the client to use the declared 
     * bean as the endpoint provider instance for the client.
     * 
     * @return The endpoint provider bean reference.
     */
    String endpointProvider() default "endpointProvider";

}
