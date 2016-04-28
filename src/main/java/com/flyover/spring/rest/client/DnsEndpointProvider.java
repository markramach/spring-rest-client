/**
 * 
 */
package com.flyover.spring.rest.client;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.spotify.dns.DnsSrvResolver;
import com.spotify.dns.DnsSrvResolvers;

/**
 * @author mramach
 *
 */
public class DnsEndpointProvider implements EndpointProvider {
    
    private DnsSrvResolver resolver;
    
    public DnsEndpointProvider() {
        
        resolver = DnsSrvResolvers.newBuilder()
                .cachingLookups(true)
                .retainingDataOnFailures(true)
                .dnsLookupTimeoutMillis(3000)
                    .build();
        
    }

    @Override
    public List<Endpoint> resolve(String serviceName) {
        
        String scheme = Pattern.compile(".*\\.?\\_ssl\\..*")
                .matcher(serviceName).matches() ? "https" : "http";
        
        return resolver.resolve(serviceName).stream()
            .map(r -> new Endpoint(r.host(), r.port(), scheme))
                .collect(Collectors.toList());
        
    }

    public void setResolver(DnsSrvResolver resolver) {
        this.resolver = resolver;
    }

}
