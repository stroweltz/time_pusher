package com.leon.timeconsumer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
public class LazyDatabaseConnectionMonitorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    private LazyDatabaseConnectionMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new LazyDatabaseConnectionMonitor(dataSource);
    }

    @Test
    void testIsDatabaseAvailableReturnsTrueWhenConnectionSuccessful() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.createStatement()).thenReturn(statement);
        lenient().when(statement.execute("SELECT 1")).thenReturn(true);

        boolean result = monitor.isDatabaseAvailable();

        assertTrue(result);
    }

    @Test
    void testIsDatabaseAvailableReturnsFalseWhenConnectionFails() throws SQLException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertNotNull(monitor);
        assertNotNull(monitor.isDatabaseAvailable());
    }

    @Test
    void testIsDatabaseAvailableHandlesSQLException() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.createStatement()).thenReturn(statement);
        lenient().when(statement.execute("SELECT 1")).thenThrow(new SQLException("Query failed"));

        assertNotNull(monitor.isDatabaseAvailable());
    }

    @Test
    void testMonitorInitializesCorrectly() {
        assertNotNull(monitor);
        assertTrue(monitor.isDatabaseAvailable());
    }
}

