package com.execute.quotes;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.impl.QuotesLive;
import com.interfaces.RequestData;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Map;
import java.util.Set;

/**
 * Created by dima on 07.05.18.
 */
public class RequestQuotesFinam extends RequestData<QuotesLive> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    @Value("${service.persist}")
    private String persistService;

    @Value("${currency.quotes}")
    protected String MAIN;

    @Override
    public Map<String, Object> getRequest(Set<QuotesCriteriaBuilder> criteriaBuilders) {
        localResp.clear();
        mapResp.clear();
//        criteriaBuilders
        return null;
    }

    private void persist(Map<String, Object> request){
        Application application = discoveryClient.getApplication(persistService);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/" + "quotes/";
        restTemplate.put(url,request.get("successful"));
    }
}
