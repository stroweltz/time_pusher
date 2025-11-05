package com.leon.timeconsumer.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Slf4j
@Getter
public class LazyDatabaseConnectionMonitor {

    private final DataSource dataSource;
    private volatile boolean isDatabaseAvailable = true;
    private Thread monitorThread;
    @Value("${app.db.monitor-interval-ms:5000}")
    private long monitorIntervalMs;

    public LazyDatabaseConnectionMonitor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isDatabaseAvailable() {
        return isDatabaseAvailable;
    }

    @PostConstruct
    private void startMonitoring() {
        monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    boolean current = checkDatabaseConnection();
                    if (isDatabaseAvailable != current) {
                        isDatabaseAvailable = current;
                        if (current) {
                            log.info("Database is back online");
                        } else {
                            log.warn("Database connection lost");
                        }
                    }
                    Thread.sleep(monitorIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in database monitoring", e);
                }
            }
            log.info("Database monitoring stopped");
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("db-connection-monitor");
        monitorThread.start();
        log.info("Database monitoring started (interval {} ms)", monitorIntervalMs);
    }

    private boolean checkDatabaseConnection() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            log.debug("Database health check failed: {}", e.getMessage());
            return false;
        }
    }

    @PreDestroy
    public void destroy() {
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread = null;
        }
    }
}
