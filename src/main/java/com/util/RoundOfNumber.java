package com.util;

import java.math.BigDecimal;

/**
 * Created by dima on 07.05.18.
 */
public class RoundOfNumber {

    public static BigDecimal round(String number){

        BigDecimal localNumber = new BigDecimal(number);
        localNumber=
                localNumber.compareTo(new BigDecimal(100)) > 0 ?
                        localNumber.setScale(3, BigDecimal.ROUND_HALF_UP) : localNumber.setScale(4, BigDecimal.ROUND_HALF_UP);

        return localNumber;
    }
}
