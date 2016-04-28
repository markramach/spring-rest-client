# spring-rest-client
Dynamic Spring based REST client supporting service lookup via DNS SRV records.

## Getting Started
The easiest way to get started is to create an interface to represent the dynamic REST client. This will be used to create a proxy to the dynamic REST client implementation. Then, you can simply annotate the client interface as you would an actual service implementation.

    import com.flyover.spring.rest.client.RestClient;
    
	@RestClient(value = "service._tcp.skydns.com")
	public interface ServiceClient {
	    
	    @RequestMapping("/path/{id}")
    	Map<String, String> get(@PathVariable("id") String id);
    	
    	@RequestMapping(value = "/path", method = RequestMethod.POST)
        Map<String, String> post(@RequestBody Map<String, String> body);
	
	}

After creating your client interface, you will need to enable supporting dynamic REST client beans to detect your interface and create the client proxy.

    @Configuration
    @EnableRestClients
    public class RestClientConfiugration {
        
        ...
        // Other Configuration
        ...
        
    }
    
Using the `@EnableRestClients` annotation instructs the context to initialize all the required spring components and scan the configuration class's base package for any interfaces annotated with the `@RestClient` annotation. If this is successful, you should be able to simply inject your client instance into any spring bean using `@Autowired`.

    @Autowired
    private ServiceClient client;
    
## DNS SRV Resolution
Most people are familiar with DNS. However, many are not familiar with the DNS record type SRV. This record type allows for the definition of a service name and port number. As an example I'm going to create a few simple SRV records using [skydns](https://github.com/skynetservices/skydns).

First, I'm going to add a few entries to the backing [etcd](https://github.com/coreos/etcd) instance.

    $ curl -XPUT http://192.168.59.103:4001/v2/keys/skydns/local/skydns/_tcp/service/1 -d value='{"host":"10.10.10.2","port":8080}'
    {"action":"set","node":{"key":"/skydns/local/skydns/_tcp/service/1","value":"{\"host\":\"10.10.10.2\",\"port\":8080}","modifiedIndex":35751,"createdIndex":35751}}

    $ curl -XPUT http://192.168.59.103:4001/v2/keys/skydns/local/skydns/_tcp/service/2 -d value='{"host":"10.10.10.3","port":8080}'
    {"action":"set","node":{"key":"/skydns/local/skydns/_tcp/service/2","value":"{\"host\":\"10.10.10.3\",\"port\":8080}","modifiedIndex":35752,"createdIndex":35752}}

    $ curl -XPUT http://192.168.59.103:4001/v2/keys/skydns/local/skydns/_tcp/service/3 -d value='{"host":"10.10.10.4","port":8080}'
    {"action":"set","node":{"key":"/skydns/local/skydns/_tcp/service/3","value":"{\"host\":\"10.10.10.4\",\"port\":8080}","modifiedIndex":35753,"createdIndex":35753}}
    
Now, if we lookup our service from sky DNS we get all the available endpoints, plus the port values.

    $ dig @192.168.59.103 +short service._tcp.skydns.local SRV
    10 33 8080 1.service._tcp.skydns.local.
    10 33 8080 2.service._tcp.skydns.local.
    10 33 8080 3.service._tcp.skydns.local.
    
Now, lets add an additional endpoint an an alternate port.

    $ curl -XPUT http://192.168.59.103:4001/v2/keys/skydns/local/skydns/_tcp/service/4 -d value='{"host":"10.10.20.2","port":8282}'
    {"action":"set","node":{"key":"/skydns/local/skydns/_tcp/service/4","value":"{\"host\":\"10.10.20.2\",\"port\":8282}","modifiedIndex":35754,"createdIndex":35754}}
    
Again a lookup of the service should yield all SRV records.

    dig @192.168.59.103 +short service._tcp.skydns.local SRV
    10 25 8080 3.service._tcp.skydns.local.
    10 25 8282 4.service._tcp.skydns.local.
    10 25 8080 1.service._tcp.skydns.local.
    10 25 8080 2.service._tcp.skydns.local.
    
Also notice with skydns, running a lookup multiple times results in the result set returning in a round-robin cycle.

### Rest Client Service Lookup
The advantage of using DNS as a service registry is the fact that DNS is a well established protocol supported by almost every OS running today. Why not take advantage. This dynamic REST client utilizes DNS SRV records via an injected component called `DnsEndpointProvider`. This class uses an open source [DNS](https://github.com/spotify/dns-java) library provided by spotify. Based on the load balancer type selected, the set of endpoints is refreshed at a regular interval. This allows new service instances to be added and removed with minimal disruption to the client.

## Additional Configuration

### Alternate Client Package
If you have place client interfaces in a package that is not the same as your configuration class, you can specify that package using the value attribute of the `@EnableRestClients` annotation.

    @EnableRestClients("com.package.name")
    public static class RestClientConfiugration {
        ...
    }
    
### Load Balancers
There are 2 basic load balancer implementations. The default `RoundRobinLoadBalancer` and the `FirstAvailableLoadBalancer`. The `RoundRobinLoadBalancer` attempts to continuously iterate all the available service endpoints, while the `FirstAvailableLoadBalancer` simply selects the first endpoint available in the list. You can set the load balancer type to use for a client by specifying the type in the `@RestClient` annotation. The `RoundRobinLoadBalancer` will be used as the default when not specified.

    @RestClient(value = "service._tcp.skydns.com", loadBalancer = FirstAvailableLoadBalancer.class)
	public interface ServiceClient {
	    ...
    }
    
### Endpoint Provider
If you would like to change out the endpoint provider implementation to use anther source, you can simply implement your own `EndpointProvider` instance and configure your clients to use that instead. You can specify the override value using the endpointProvider attribute on the `@RestClient` annotation.

    @Configuration
    @EnableRestClients
    public static class TestConfiugration {
        
        @Bean
        public EndpointProvider customEndpointProvider() {
            return new EndpointProvider() {
                public List<Endpoint> resolve(String serviceName) {
                    return Arrays.asList(new Endpoint("localhost", 8080, "http"));
                }
            };
        }
        
    }

    @RestClient(value = "service._tcp.skydns.com", endpointProvider = "customEndpointProvider")
	public interface ServiceClient {
	    ...
    }