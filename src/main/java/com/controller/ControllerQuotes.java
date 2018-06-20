package com.controller;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.enums.Currency;
import com.dim.fxapp.entity.enums.Period;
import com.execute.quotes.RequestQuotesFinam;
import com.interfaces.RequestData;
import com.util.DevideDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by dima on 07.05.18.
 */
@RestController
@RequestMapping(value = "/quotes")
public class ControllerQuotes {

    @Autowired
    private RequestQuotesFinam quotes;

    @RequestMapping(value = "/reload", method = RequestMethod.GET)
    public void reload() {
        LocalDate.now().minusYears(10);
        LocalDate.now();

        reloadFromTo(
                LocalDate.now().minusYears(10).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        );
    }

    @RequestMapping(value = "/reloadfromexist", method = RequestMethod.GET)
    public void reloadFromExists() {
        quotes.reload();
    }

    @RequestMapping(value = "/reloadtoday", method = RequestMethod.GET)
    public void reloadToday() {
        reloadFromTo(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    @RequestMapping(value = "/reloadweek", method = RequestMethod.GET)
    public void reloadWeak() {
        reloadFromTo(
                LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    @RequestMapping(value = "/reloadmonth", method = RequestMethod.GET)
    public void reloadMonth() {
        reloadFromTo(
                LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }


    @RequestMapping(value = "/reload/{from}/{to}", method = RequestMethod.GET)
    public void reloadFromTo(@PathVariable("from") String from, @PathVariable("from") String to) {
        Set<Currency> setOfCurrency = new LinkedHashSet<>(Arrays.asList(Currency.values()));
        Set<Period> setOfPeriod = new LinkedHashSet<>(Arrays.asList(Period.values()));
        Set<QuotesCriteriaBuilder> criteriaBuilderSet = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate start = LocalDate.from(LocalDate.parse(from, formatter).atStartOfDay());
        LocalDate end = LocalDate.from(LocalDate.parse(to, formatter).atStartOfDay());
        List<LocalDate> localDateList = DevideDate.devideDate(start, end);

        for(Currency currency: setOfCurrency){
            for(Period period: setOfPeriod){
                for (int i = 0; i < localDateList.size() - 1; i++) {
                    criteriaBuilderSet.add(getCriteria(currency, period, localDateList.get(i), localDateList.get(i + 1)));
                }
            }
        }
        quotes.getRequest(criteriaBuilderSet);
    }


    private QuotesCriteriaBuilder getCriteria(Currency currency, Period period, LocalDate from, LocalDate to) {
        QuotesCriteriaBuilder quotesCriteriaBuilder = QuotesCriteriaBuilder.builder()
                .currency(currency)
                .period(period)
                .from(from)
                .to(to)
                .build();
        return quotesCriteriaBuilder;
    }
}