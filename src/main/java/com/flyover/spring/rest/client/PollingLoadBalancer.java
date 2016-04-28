/**
 * 
 */
package com.flyover.spring.rest.client;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author mramach
 *
 */
public class PollingLoadBalancer extends AbstractLoadBalancer {
    
    @Autowired(required=false)
    private List<ClientRequestInterceptor> interceptors = new LinkedList<>();
    private ScheduledThreadPoolExecutor executor;
    
    @PostConstruct
    public void init() {
        
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::setEndpoint, 0, 15, TimeUnit.SECONDS);
        
    }
    
    @PreDestroy
    public void destroy() {
        
        try {
            
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
            
        } catch (InterruptedException e) {
            // Do nothing. Simply shutting down the thread pool.
        }
        
    }
    
    public <T> T execute(Method method, Object... args) {
        return new DynamicRestClient(interceptors).execute(getEndpoint(), method, args);
    }
    
    protected void setEndpoint() {
        throw new UnsupportedOperationException();
    }
    
    protected Endpoint getEndpoint() {
        throw new UnsupportedOperationException();
    }

}
