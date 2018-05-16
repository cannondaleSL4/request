package com;

import com.execute.livequotes.RequestLiveQuotesOldVersion;
import com.execute.quotes.RequestQuotesFinam;
import com.interfaces.RequestData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Created by dima on 21.01.18.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class RequestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestApplication.class, args) ;
    }

    @Bean("LiveQuotesOldVersion")
    public RequestData getLiveQuotesOldVersion(){
        return new RequestLiveQuotesOldVersion();
    }

    @Bean("Quotes")
    public RequestData getQuotes(){
        return new RequestQuotesFinam();
    }

    @Bean
    public RestTemplate getRestTemplate(){return new RestTemplate();}

}
