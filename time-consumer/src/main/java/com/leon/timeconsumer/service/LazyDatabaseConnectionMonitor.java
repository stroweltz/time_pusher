package com.leon.timeconsumer.service;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Slf4j
@Getter
public class LazyDatabaseConnectionMonitor {

    private final DataSource dataSource;
    private final Object connectionMonitor = new Object();
    private volatile boolean isDatabaseAvailable = true;
    private volatile boolean isMonitoring = false;
    private Thread monitorThread;

    public LazyDatabaseConnectionMonitor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isDatabaseAvailable() {
        if (!isMonitoring) {
            return checkDatabaseConnection();
        }
        return isDatabaseAvailable;
    }

    public void waitForDatabaseRecovery() throws InterruptedException {
        startMonitoring();

        synchronized (connectionMonitor) {
            while (!isDatabaseAvailable) {
                connectionMonitor.wait(30000); // Ждем до 30 секунд
            }
        }
    }

    private synchronized void startMonitoring() {
        if (isMonitoring) return;

        isMonitoring = true;
        monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && isMonitoring) {
                try {
                    boolean previousState = isDatabaseAvailable;
                    isDatabaseAvailable = checkDatabaseConnection();

                    if (!previousState && isDatabaseAvailable) {
                        log.info("Database is back online! Notifying all waiting threads");
                        synchronized (connectionMonitor) {
                            connectionMonitor.notifyAll();
                        }
                        stopMonitoring(); // Выключаем мониторинг когда БД восстановилась
                    } else if (previousState && !isDatabaseAvailable) {
                        log.warn("Database connection lost! Starting active monitoring");
                    }

                    Thread.sleep(5000);

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
        log.info("Database monitoring started");
    }

    private synchronized void stopMonitoring() {
        isMonitoring = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread = null;
        }
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
        stopMonitoring();
    }
}
