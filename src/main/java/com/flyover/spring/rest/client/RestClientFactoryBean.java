/**
 * 
 */
package com.flyover.spring.rest.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author mramach
 *
 */
@SuppressWarnings("unchecked")
public class RestClientFactoryBean<T> extends AbstractFactoryBean<T> {

    private LoadBalancer loadBalancer;
    private Class<T> clientInterface;
    private T proxyInstance;

    @Override
    public Class<?> getObjectType() {
        return clientInterface;
    }

    @Override
    protected T createInstance() throws Exception {
        
        if(proxyInstance == null) {
            
            proxyInstance = (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), 
                    new Class<?>[]{clientInterface}, this::invoke);
            
        }
        
        return (T) proxyInstance;
        
    }
    
    private Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return loadBalancer.execute(method, args);
    }

    public void setClientInterface(Class<T> clientInterface) {
        this.clientInterface = clientInterface;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

}
