package com.globalbank.bookentry.util;

import java.math.BigDecimal;

public class ValidationUtil {

    public static boolean isAmountValid(BigDecimal amount) {
        return BigDecimal.ZERO.compareTo(amount) <= 0;
    }
}
