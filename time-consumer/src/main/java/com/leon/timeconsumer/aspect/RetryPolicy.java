package com.leon.timeconsumer.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryPolicy {
    int maxAttempts() default 3;
    Class<? extends Exception>[] retryExceptions() default {};
    Class<? extends Exception>[] infiniteRetryExceptions() default {};
}
