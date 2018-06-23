package com.controller;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.execute.quotes.RequestQuotesFinam;
import com.services.CreteriaService;
import org.springframework.beans.factory.annotation.Autowired;
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


    private RequestQuotesFinam quotes;
    private CreteriaService creteriaService;

    @Autowired
    public ControllerQuotes(RequestQuotesFinam quotes, CreteriaService creteriaService){
        this.quotes = quotes;
        this.creteriaService = creteriaService;
    }


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
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        if (from.getDayOfWeek().getValue() != 6  &&
                from.getDayOfWeek().getValue() != 7 ){
            reloadFromTo(
                    from.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    to.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        }
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
        Set<QuotesCriteriaBuilder> criteriaBuilderSet = creteriaService.getCriteria(from,to);
        quotes.getRequest(criteriaBuilderSet);
    }

}