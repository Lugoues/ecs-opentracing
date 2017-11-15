// Based on https://github.com/binblee/zipkin-demo
package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
public class TracedemoApplication {

    private static final Logger LOG = Logger.getLogger(TracedemoApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(TracedemoApplication.class, args);
	}

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @RequestMapping("/")
    public String callHome(){
        LOG.log(Level.INFO, "calling trace demo");
        return "Hello from Tracedemo application " + new Date().toString();
    }

    @RequestMapping("/backend")
    public String callBackend(){
        LOG.log(Level.INFO, "calling trace demo backend");
        return restTemplate.getForObject("http://backend:8090", String.class);
    }

    @Bean
    public AlwaysSampler defaultSampler(){
        return new AlwaysSampler();
    }

}
