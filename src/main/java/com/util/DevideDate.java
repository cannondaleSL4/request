package com.util;

import com.dim.fxapp.entity.enums.Period;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dima on 27.05.18.
 */
public class DevideDate {
    public static List<LocalDate> devideDate(LocalDate from, LocalDate to){
        List<LocalDate> listOfDate = new ArrayList<>();
        while (from.isBefore(to)){
            listOfDate.add(from);
            from = from.plusYears(1);
        }
        listOfDate.add(to);
        return listOfDate;
    }
}
