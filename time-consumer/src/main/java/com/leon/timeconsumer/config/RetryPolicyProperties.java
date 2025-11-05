package com.leon.timeconsumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "retry.policy")
@Data
public class RetryPolicyProperties {
    private long databaseCheckIntervalMs = 5000;
    private long maxBackoffMs = 30000;
    private long initialBackoffMs = 1000;
}
