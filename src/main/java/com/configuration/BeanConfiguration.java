package com.configuration;

import com.execute.livequotes.RequestLiveQuotesOldVersion;
import com.execute.quotes.RequestQuotesFinam;
import com.interfaces.RequestData;
import com.services.CriteriaService;
import com.services.TimerService;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by dima on 23.06.18.
 */

@Configuration
public class BeanConfiguration {
    @Bean("LiveQuotesOldVersion")
    public RequestData getLiveQuotesOldVersion() {
        return new RequestLiveQuotesOldVersion();
    }

    @Bean
    public RequestQuotesFinam getQuotes() {
        return new RequestQuotesFinam();
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CriteriaService getCriteriaService() {
        return new CriteriaService();
    }

    @Bean
    public TimerService getTimerService() {
        return new TimerService(getQuotes(), getCriteriaService());
    }

    @Bean
    public RestartEndpoint getRestart(){ return new RestartEndpoint();}
}
