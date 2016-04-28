/**
 * 
 */
package com.flyover.spring.rest.client;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Iterators;

/**
 * @author mramach
 *
 */
public class RoundRobinLoadBalancer extends PollingLoadBalancer {
    
    private Iterator<Endpoint> iterator = Collections.emptyIterator();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    protected void setEndpoint() {
        
        lock.writeLock().lock();
        
        try {
            
            iterator = Iterators.cycle(getEndpointProvider().resolve(getServiceName()));
            
        } finally {
            lock.writeLock().unlock();
        }
        
    }

    protected Endpoint getEndpoint() {
        
        lock.readLock().lock();
        
        try {
            
            if(!iterator.hasNext()) {
                throw new RuntimeException("No service endpoints available.");
            }
            
            return iterator.next();
            
        } finally {
            lock.readLock().unlock();
        }
        
    }
    
}
