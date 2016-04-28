# spring-rest-client
Dynamic Spring based REST client supporting service lookup via DNS SRV records.

## Getting Started
The easiest way to get started is to create an interface to represent the dynamic REST client. This will be used to create a proxy to the dynamic REST client implementation. Then, you can simply annotate the client interface as you would an actual service implementation.

    import com.flyover.spring.rest.client.RestClient;
    
	@RestClient(value = "service._tcp.domain.com")
	public interface ServiceClient {
	    
	    @RequestMapping("/path/{id}")
    	Map<String, String> get(@PathVariable("id") String id);
	
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
    
