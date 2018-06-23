package com.controller;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.enums.Currency;
import com.dim.fxapp.entity.enums.Period;
import com.util.DevideDate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CreteriaService {

    public Set<QuotesCriteriaBuilder> getCriteria(String from, String to) {
        Set<Currency> setOfCurrency = new LinkedHashSet<>(Arrays.asList(Currency.values()));
        Set<Period> setOfPeriod = new LinkedHashSet<>(Arrays.asList(Period.values()));
        Set<QuotesCriteriaBuilder> criteriaBuilderSet = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate start = LocalDate.from(LocalDate.parse(from, formatter).atStartOfDay());
        LocalDate end = LocalDate.from(LocalDate.parse(to, formatter).atStartOfDay());
        List<LocalDate> localDateList = DevideDate.devideDate(start, end);

        for (Currency currency : setOfCurrency) {
            for (Period period : setOfPeriod) {
                for (int i = 0; i < localDateList.size() - 1; i++) {
                    criteriaBuilderSet.add(getCriteria(currency, period, localDateList.get(i), localDateList.get(i + 1)));
                }
            }
        }

        return criteriaBuilderSet;
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
