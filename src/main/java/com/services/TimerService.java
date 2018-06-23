package com.services;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.execute.quotes.RequestQuotesFinam;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class TimerService {

    private final CreteriaService creteriaService;
    private final RequestQuotesFinam requestQuotesFinam;

    @Autowired
    public TimerService(RequestQuotesFinam requestQuotesFinam,CreteriaService creteriaService){
        this.requestQuotesFinam = requestQuotesFinam;
        this.creteriaService = creteriaService;

        /**every 2 hours - 7200000 millisec */
        Timer timer = new Timer();
        timer.schedule(new RemindTask(), 0,7200000);
    }

    class RemindTask extends TimerTask {
        @Override
        public void run() {

            Set<QuotesCriteriaBuilder> criteriaBuilders;

            LocalDate from = LocalDate.now();
            LocalDate to = LocalDate.now().plusDays(1);

            if (from.getDayOfWeek().getValue() != 6  &&
                    from.getDayOfWeek().getValue() != 7 ){
                criteriaBuilders = creteriaService.getCriteria(from.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        to.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                requestQuotesFinam.getRequest(criteriaBuilders);
            }
        }
    }
}
