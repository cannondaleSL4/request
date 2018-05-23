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
    @Qualifier("Quotes")
    private RequestData quotes;

    @RequestMapping(value="/reload", method = RequestMethod.GET)
    public void reload(){

        List<Currency> listOfCurrency = new ArrayList<>(Arrays.asList(Currency.values()));
        List<LocalDate> localDateList = devideDate(LocalDate.now().minusYears(5),
                LocalDate.now());

        Set<QuotesCriteriaBuilder> criteriaBuilderSet = new HashSet<>();

        listOfCurrency.forEach(currency -> {
            localDateList.forEach(localDate -> {
                for(int i=0; i < localDateList.size()-1;i++){
                    criteriaBuilderSet.add(getCriteria(currency, localDateList.get(i),localDateList.get(i+1)));
                }
            });
        });
        quotes.getRequest(criteriaBuilderSet);
    }


    private List<LocalDate> devideDate(LocalDate from,LocalDate to){
        List<LocalDate> listOfDate = new ArrayList<>();
        while (from.isBefore(to)){
            listOfDate.add(from);
            from = from.plusMonths(1);
        }
        listOfDate.add(to);
        return listOfDate;
    }

    private QuotesCriteriaBuilder getCriteria(Currency currency, LocalDate from, LocalDate to){
        QuotesCriteriaBuilder quotesCriteriaBuilder = QuotesCriteriaBuilder.builder()
            .currency(currency)
            .from(from)
            .to(to)
            .build();
        return quotesCriteriaBuilder;
    }
}


//https://dzone.com/articles/microservices-communication-service-to-service