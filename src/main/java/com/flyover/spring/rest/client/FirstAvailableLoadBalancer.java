/**
 * 
 */
package com.flyover.spring.rest.client;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author mramach
 *
 */
public class FirstAvailableLoadBalancer extends PollingLoadBalancer {
    
    private Optional<Endpoint> endpoint = Optional.empty();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    protected void setEndpoint() {
        
        lock.writeLock().lock();
        
        try {

            endpoint = getEndpointProvider().resolve(getServiceName()).stream().findFirst();
            
        } finally {
            lock.writeLock().unlock();
        }
        
    }

    protected Endpoint getEndpoint() {
        
        lock.readLock().lock();
        
        try {
            
            return endpoint.orElseThrow(() -> new RuntimeException("No service endpoints available."));
            
        } finally {
            lock.readLock().unlock();
        }
        
    }
    
}
