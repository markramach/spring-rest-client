/**
 * 
 */
package com.flyover.spring.rest.client;

/**
 * @author mramach
 *
 */
public class Endpoint {
    
    private String address;
    private Integer port;
    private String scheme;
    
    public Endpoint(String address, Integer port, String scheme) {
        this.address = address;
        this.port = port;
        this.scheme = scheme;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public String getScheme() {
        return scheme;
    }

}
