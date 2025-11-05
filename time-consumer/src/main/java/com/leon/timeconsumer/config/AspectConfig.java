package com.leon.timeconsumer.config;

import com.leon.timeconsumer.aspect.RetryPolicyAspect;
import com.leon.timeconsumer.service.LazyDatabaseConnectionMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {

    @Bean
    public RetryPolicyAspect retryPolicyAspect(LazyDatabaseConnectionMonitor dbMonitor, RetryPolicyProperties properties) {
        return new RetryPolicyAspect(dbMonitor, properties);
    }
}
