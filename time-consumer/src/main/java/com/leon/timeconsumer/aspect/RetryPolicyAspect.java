package com.leon.timeconsumer.aspect;

import com.leon.timeconsumer.config.RetryPolicyProperties;
import com.leon.timeconsumer.service.LazyDatabaseConnectionMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor

public class RetryPolicyAspect {

    private final LazyDatabaseConnectionMonitor dbMonitor;
    private final RetryPolicyProperties properties;

    @Around("@annotation(retryPolicy)")
    public Object applyRetryPolicy(ProceedingJoinPoint joinPoint, RetryPolicy retryPolicy) throws Throwable {
        int attempt = 0;
        long lastDatabaseCheck = 0;
        boolean processed = false;
        Object result = null;

        while (!processed) {
            attempt++;

            try {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDatabaseCheck >= properties.getDatabaseCheckIntervalMs()) {
                    waitForDatabase(attempt);
                    lastDatabaseCheck = currentTime;
                }

                result = joinPoint.proceed();
                processed = true;
                log.info("Operation completed successfully on attempt {}", attempt);

            } catch (Exception e) {
                if (shouldRetryInfinitely(e, retryPolicy.infiniteRetryExceptions())) {
                    log.warn("Infinite retry exception on attempt {}, retrying...", attempt, e);
                    waitWithBackoff(attempt);

                } else if (shouldRetryWithLimit(e, attempt, retryPolicy)) {
                    log.warn("Retryable exception on attempt {}/{}, retrying...",
                            attempt, retryPolicy.maxAttempts(), e);
                    waitWithBackoff(attempt);

                } else {
                    log.error("Non-retryable exception on attempt {}, giving up", attempt, e);
                    throw e;
                }
            }
        }

        return result;
    }

    private boolean shouldRetryInfinitely(Exception e, Class<? extends Exception>[] infiniteRetryExceptions) {
        return Arrays.stream(infiniteRetryExceptions)
                .anyMatch(exClass -> exClass.isInstance(e));
    }

    private boolean shouldRetryWithLimit(Exception e, int attempt, RetryPolicy retryPolicy) {
        boolean isRetryableException = Arrays.stream(retryPolicy.retryExceptions())
                .anyMatch(exClass -> exClass.isInstance(e));
        return isRetryableException && attempt < retryPolicy.maxAttempts();
    }


    private void waitForDatabase(int attempt) {
        while (!dbMonitor.isDatabaseAvailable()) {
            log.warn("Database not connected, attempt {}. Waiting...", attempt);
            waitWithBackoff(attempt);
        }
    }

    private void waitWithBackoff(int attempt) {
        try {
            long backoff = Math.min(
                    properties.getInitialBackoffMs() * (long) Math.pow(2, attempt - 1),
                    properties.getMaxBackoffMs()
            );
            Thread.sleep(backoff);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during backoff", e);
        }
    }
}