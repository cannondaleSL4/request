package com.execute.quotes;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.impl.QuotesLive;
import com.interfaces.RequestData;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Map;
import java.util.Set;

/**
 * Created by dima on 07.05.18.
 */
public class RequestQuotesFinam extends RequestData<QuotesLive> {

    @Value("${currency.quotes}")
    protected String MAIN;

    @Override
    public Map<String, Object> getRequest(Set<QuotesCriteriaBuilder> criteriaBuilders) {
        return null;
    }
}
