/**
 * 
 */
package com.flyover.spring.rest.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Indicates that the annotated interface is a dynamic Consul Client interface.
 * 
 * @author mramach
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RestClientBeanDefinitionRegistrar.class)
public @interface EnableRestClients {

    String value() default "";
    
}
