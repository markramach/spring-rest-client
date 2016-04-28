/**
 * 
 */
package com.flyover.spring.rest.client;

import java.util.List;

/**
 * @author mramach
 *
 */
public interface EndpointProvider {
    
    List<Endpoint> resolve(String serviceName);

}
