/**
 * 
 */
package com.flyover.spring.rest.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;


/**
 * @author mramach
 *
 */
public class RestClientBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    
    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    
    private ResourcePatternResolver resourcePatternResolver = 
            new PathMatchingResourcePatternResolver();
    
    private MetadataReaderFactory metadataReaderFactory = 
            new CachingMetadataReaderFactory(this.resourcePatternResolver);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableRestClients.class.getName());
        
        String basePackage = (String) attributes.get("value");
        
        basePackage = StringUtils.hasLength(basePackage) ? 
                basePackage : ClassUtils.getPackageName(metadata.getClassName());
        
        String basePath = ClassUtils.convertClassNameToResourcePath(basePackage);
    
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                basePath + DEFAULT_RESOURCE_PATTERN;
        
        Arrays.stream(getResources(packageSearchPath))
            .filter(this::isCandidateIterface)
                .forEach(resource -> registerBeanDefinitions(registry, resource));
        
    }
    
    private void registerBeanDefinitions(BeanDefinitionRegistry registry, Resource resource) {
        
        try {
            
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            
            Class<?> clientInterface = ClassUtils.forName(metadataReader.getClassMetadata().getClassName(), 
                    Thread.currentThread().getContextClassLoader());
            
            RestClient annotation = clientInterface.getAnnotation(RestClient.class);
            String endpointProviderRef = annotation.endpointProvider();
            String loadBalancerRef = UUID.randomUUID().toString();

            if(!StringUtils.hasText(endpointProviderRef)) {

                endpointProviderRef = UUID.randomUUID().toString();
                
                registry.registerBeanDefinition(endpointProviderRef, BeanDefinitionBuilder
                    .genericBeanDefinition(DnsEndpointProvider.class)
                        .getBeanDefinition());
                
            }
            
            registry.registerBeanDefinition(loadBalancerRef, BeanDefinitionBuilder
                    .genericBeanDefinition(annotation.loadBalancer())
                        .addPropertyValue("annotation", annotation)
                        .addPropertyValue("serviceName", annotation.value())
                        .addPropertyReference("endpointProvider", endpointProviderRef)
                            .getBeanDefinition());
            
            registry.registerBeanDefinition(UUID.randomUUID().toString(), BeanDefinitionBuilder
                    .genericBeanDefinition(RestClientFactoryBean.class)
                        .addPropertyValue("clientInterface", clientInterface)
                        .addPropertyReference("loadBalancer", loadBalancerRef)
                            .getBeanDefinition());
            
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("Failure during classpath scanning", e);
        }
        
    }
    
    private boolean isCandidateIterface(Resource resource) {
        
        try {
            
            return metadataReaderFactory.getMetadataReader(resource)
                    .getAnnotationMetadata().hasAnnotation(RestClient.class.getName());
            
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", e);
        }
        
    }
    
    private Resource[] getResources(String packageSearchPath) {
        
        try {
            
            return resourcePatternResolver.getResources(packageSearchPath);
            
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", e);
        }
        
    }
    
}
