package com.controller;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.enums.Currency;
import com.interfaces.RequestData;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by dima on 07.05.18.
 */
@RestController
@RequestMapping(value = "/quotes")
public class ControllerQuotes {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    @Autowired
    @Qualifier("Quotes")
    private RequestData quotes;

    @Value("${service.persist}")
    private String persistService;

    public List<Currency> listOfCurrency = new ArrayList<>(Arrays.asList(Currency.values()));
    public Set<QuotesCriteriaBuilder> quotesCriteriaBuilders = new HashSet<>();

    @RequestMapping(value="/reload", method = RequestMethod.GET)
    public void reload(){

        Application application = discoveryClient.getApplication(persistService);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/" + "quotes/";

        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to = LocalDate.now();

        List<Currency> listOfCurrency = new ArrayList<>(Arrays.asList(Currency.values()));
        Set<QuotesCriteriaBuilder> quotesCriteriaBuilders = new HashSet<>();

        listOfCurrency.forEach(currency ->{
            QuotesCriteriaBuilder quotesCriteriaBuilder = QuotesCriteriaBuilder.builder()
                    .currency(currency)
                    .from(from)
                    .to(to)
                    .build();
            quotesCriteriaBuilders.add(quotesCriteriaBuilder);
        });

        quotesCriteriaBuilders.forEach(quotesCriteriaBuilder -> {
            //restTemplate.execute()
        });


//        return ResponseEntity.ok()
//                .cacheControl(CacheControl.maxAge(600, TimeUnit.SECONDS))
//                .body(quotes.getRequest(quotesCriteriaBuilders));
    }
}
