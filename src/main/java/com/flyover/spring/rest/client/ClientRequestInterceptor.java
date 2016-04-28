/**
 * 
 */
package com.flyover.spring.rest.client;

import org.springframework.http.HttpHeaders;

/**
 * @author mramach
 *
 */
public interface ClientRequestInterceptor {
    
    void execute(HttpHeaders headers);

}
