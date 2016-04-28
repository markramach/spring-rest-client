/**
 * 
 */
package com.flyover.spring.rest.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * @author mramach
 *
 */
@SuppressWarnings("unchecked")
public class DynamicRestClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRestClient.class);

    List<ClientRequestInterceptor> interceptors;
    
    public DynamicRestClient(List<ClientRequestInterceptor> interceptors) {
        Assert.notNull(interceptors, "Interceptor list cannot be null.");
        this.interceptors = interceptors;
    }

    public <T> T execute(Endpoint endpoint, Method m, Object...args) {

        RequestMapping mapping = m.getAnnotation(RequestMapping.class);
        Parameters parameters = new Parameters(m, args);
        
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(parameters.getRequestHeaders());
        
        URI uri = UriComponentsBuilder.newInstance()
                .scheme(endpoint.getScheme())
                .host(endpoint.getAddress())
                .port(endpoint.getPort())
                .path(mapping.value()[0])
                        .queryParams(new LinkedMultiValueMap<String, String>(parameters.getRequestParams()))
                                .buildAndExpand(parameters.getUriVariables())
                                        .toUri();
        
        interceptors.forEach(i -> i.execute(headers));
        
        try {
            
            return (T) new RestTemplate().exchange(uri, getHttpMethod(mapping), 
                    new HttpEntity<>(parameters.getRequestBody(), headers), m.getReturnType()).getBody();
            
        } catch (HttpClientErrorException e) {
            LOGGER.error(e.getResponseBodyAsString());
            throw e;
        }
        
    }
    
    private HttpMethod getHttpMethod(RequestMapping mapping) {
        
        if(mapping.method().length > 1) {
            
            throw new RuntimeException("More than one HTTP method declared, "
                    + "a client request method can not be determined.");
            
        }
        
        return HttpMethod.valueOf(Arrays.stream(mapping.method())
                .findFirst().orElse(RequestMethod.GET).name());
        
    }
    
    private class Parameters {

        private List<Parameter> parameters;
        
        public Parameters(Method m, Object[] args) {
            
            AtomicInteger idx = new AtomicInteger();
            
            parameters = Arrays.stream(m.getParameterAnnotations())
                .map(annotations -> new Parameter(annotations, args[idx.getAndIncrement()]))
                    .collect(Collectors.toList());
            
        }

        public Object[] getUriVariables() {

            return parameters.stream().filter(Parameter::isPathVariable).map(Parameter::getArg).toArray();
            
        }
        
        public Map<String, List<String>> getRequestHeaders() {

            return parameters.stream().filter(Parameter::isRequestHeader)
                    .collect(Collectors.toMap(Parameter::getName, (p) -> Arrays.asList((String)p.getArg())));
            
        }
        
        public Map<String, List<String>> getRequestParams() {

            return parameters.stream().filter(Parameter::isRequestParam)
                    .collect(Collectors.toMap(Parameter::getName, (p) -> Arrays.asList(String.valueOf(p.getArg()))));
            
        }
        
        public <T> T getRequestBody() {
            
            return (T) parameters.stream()
                    .filter(Parameter::isRequestBody)
                            .map(Parameter::getArg)
                                    .findFirst()
                                            .orElse(Void.TYPE);
            
        }
        
    }
    
    private class Parameter {
        
        private Annotation[] annotations;
        private Object arg;
        
        public Parameter(Annotation[] annotations, Object arg) {
            this.annotations = annotations;
            this.arg = arg;
        }

        public boolean isPathVariable() {
            
            return Arrays.stream(annotations)
                    .filter(a -> PathVariable.class.isAssignableFrom(a.annotationType()))
                            .findFirst()
                                    .isPresent();
            
        }
        
        private PathVariable getPathVariable() {
            
            return (PathVariable) Arrays.stream(annotations)
                    .filter(a -> PathVariable.class.isAssignableFrom(a.annotationType()))
                            .findFirst()
                                    .get();
            
        }

        public boolean isRequestHeader() {
            
            return Arrays.stream(annotations)
                    .filter(a -> RequestHeader.class.isAssignableFrom(a.annotationType()))
                            .findFirst()
                                    .isPresent();
            
        }
        
        private RequestHeader getRequestHeader() {
            
            return (RequestHeader) Arrays.stream(annotations)
                    .filter(a -> RequestHeader.class.isAssignableFrom(a.annotationType()))
                            .findFirst()
                                    .get();
            
        }
        
        public boolean isRequestParam() {
            
            return Arrays.stream(annotations)
                    .filter(a -> RequestParam.class.isAssignableFrom(a.annotationType()))
                            .findFirst()
                                    .isPresent();
            
        }
        
        private RequestParam getRequestParam() {
            
            return (RequestParam) Arrays.stream(annotations)
                    .filter(a -> RequestParam.class.isAssignableFrom(a.annotationType()))
                            .findFirst()
                                    .get();
            
        }
        
        public boolean isRequestBody() {
            
            return Arrays.stream(annotations)
                    .filter(a -> RequestBody.class.isAssignableFrom(a.annotationType()))
                            .findFirst()
                                    .isPresent();
            
        }
        
        public String getName() {
            
            return isRequestHeader() ? getRequestHeader().value() : isRequestParam() ? getRequestParam().value() : getPathVariable().value();
            
        }

        public Object getArg() {
            return arg;
        }
        
    }
    
}
