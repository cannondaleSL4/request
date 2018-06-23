package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by dima on 21.01.18.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class RequestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestApplication.class, args) ;
    }
}
