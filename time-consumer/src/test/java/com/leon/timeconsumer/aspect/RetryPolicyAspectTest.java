package com.leon.timeconsumer.aspect;

import com.leon.timeconsumer.config.RetryPolicyProperties;
import com.leon.timeconsumer.service.LazyDatabaseConnectionMonitor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetryPolicyAspectTest {

    @Mock
    private LazyDatabaseConnectionMonitor dbMonitor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private RetryPolicyAspect aspect;

    @BeforeEach
    void setUp() {
        RetryPolicyProperties properties = new RetryPolicyProperties();
        properties.setDatabaseCheckIntervalMs(5000L);
        properties.setMaxBackoffMs(30000L);
        properties.setInitialBackoffMs(1000L);

        aspect = new RetryPolicyAspect(dbMonitor, properties);
        when(dbMonitor.isDatabaseAvailable()).thenReturn(true);
    }

    @Test
    void shouldRetryOnDataAccessException() throws Throwable {
        when(joinPoint.proceed())
                .thenThrow(new DataAccessException("DB error") {})
                .thenReturn(null);

        RetryPolicy retryPolicy = createRetryPolicy(
                DataAccessException.class,
                ConstraintViolationException.class);

        assertDoesNotThrow(() -> aspect.applyRetryPolicy(joinPoint, retryPolicy));
        verify(joinPoint, times(2)).proceed();
        verify(dbMonitor, atLeastOnce()).isDatabaseAvailable();
    }

    @Test
    void shouldCheckDatabaseNotMoreOftenThanConfigured() throws Throwable {
        RetryPolicyProperties properties = new RetryPolicyProperties();
        properties.setDatabaseCheckIntervalMs(5000);

        RetryPolicyAspect aspect = new RetryPolicyAspect(dbMonitor, properties);

        when(dbMonitor.isDatabaseAvailable())
                .thenReturn(false)
                .thenReturn(true);

        when(joinPoint.proceed()).thenReturn(null);

        aspect.applyRetryPolicy(joinPoint, createRetryPolicy(
                DataAccessException.class,
                ConstraintViolationException.class));

        verify(dbMonitor, atMost(2)).isDatabaseAvailable();
    }

    private RetryPolicy createRetryPolicy(Class<? extends Exception> infiniteRetry,
                                          Class<? extends Exception> retry) {
        return new RetryPolicy() {
            @Override
            public int maxAttempts() { return 3; }

            @Override
            public Class<? extends Exception>[] retryExceptions() {
                return new Class[]{retry};
            }

            @Override
            public Class<? extends Exception>[] infiniteRetryExceptions() {
                return new Class[]{infiniteRetry};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RetryPolicy.class;
            }
        };
    }
}