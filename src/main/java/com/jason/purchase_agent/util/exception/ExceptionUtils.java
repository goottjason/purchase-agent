package com.jason.purchase_agent.util.exception;

import java.util.concurrent.Callable;

public class ExceptionUtils {
    public static <T> T uncheck(Callable<T> func) {
        try {
            return func.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
