/**
 * 
 */
package com.flyover.spring.rest.client;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mramach
 *
 */
@RestClient(value = "test", endpointProvider = "endpointProvider")
public interface TestRestClient {

    @RequestMapping("/test")
    Map<String, String> get();
    
    @RequestMapping("/test/{id}")
    Map<String, String> get(@PathVariable("id") String id);
    
    @RequestMapping("/test/{id}")
    Map<String, String> get(@RequestHeader("Authorization") String header, @PathVariable("id") String id);
    
    @RequestMapping("/test/{id}")
    Map<String, String> get(@RequestParam("param") String param, @RequestHeader("Authorization") String header, @PathVariable("id") String id);
    
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    Map<String, String> post(@RequestBody Map<String, String> body);
    
    @RequestMapping(value = "/ambiguous", method = {RequestMethod.GET, RequestMethod.POST})
    Map<String, String> ambiguous();
    
}
