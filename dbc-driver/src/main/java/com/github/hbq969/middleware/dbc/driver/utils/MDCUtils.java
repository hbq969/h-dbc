package com.github.hbq969.middleware.dbc.driver.utils;

import org.slf4j.MDC;

public final class MDCUtils {
    public static String rmAndGet(String key) {
        String value = MDC.get(key);
        MDC.remove(key);
        return value;
    }
}
