package com;

import com.controller.ControllerQuotes;
import com.execute.livequotes.RequestLiveQuotesOldVersion;
import com.execute.quotes.RequestQuotesFinam;
import com.interfaces.RequestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Timer;
import java.util.TimerTask;

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

    @Bean
    public RequestQuotesFinam getQuotes(){
        return new RequestQuotesFinam();
    }

    @Bean
    public RestTemplate getRestTemplate(){return new RestTemplate();}

}
