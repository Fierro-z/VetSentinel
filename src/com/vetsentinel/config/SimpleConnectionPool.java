package com.vetsentinel.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleConnectionPool {
    private final String url;
    private final List<Connection> freeConnections = new ArrayList<>();
    private final List<Connection> activeConnections = new ArrayList<>();
    private final int maxPoolSize = 10;

    public SimpleConnectionPool(String url) {
        this.url = url;
    }

    /**
     * Obtains a connection from the pool, creating a new physical connection if necessary
     * and the maxPoolSize is not reached. Returns a dynamic proxy to capture close() events.
     */
    public synchronized Connection getConnection() throws SQLException {
        // Clean up closed connections from the free pool
        freeConnections.removeIf(con -> {
            try {
                return con.isClosed();
            } catch (SQLException e) {
                return true;
            }
        });

        if (!freeConnections.isEmpty()) {
            Connection con = freeConnections.remove(freeConnections.size() - 1);
            activeConnections.add(con);
            return wrapConnection(con);
        }

        if (activeConnections.size() < maxPoolSize) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite no encontrado.", e);
            }
            Connection con = DriverManager.getConnection(url);
            try (java.sql.Statement stmt = con.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL;");
                stmt.execute("PRAGMA busy_timeout = 5000;");
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            activeConnections.add(con);
            return wrapConnection(con);
        }

        throw new SQLException("Límite máximo del pool de conexiones alcanzado (" + maxPoolSize + ").");
    }

    private Connection wrapConnection(Connection physicalConnection) {
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if (method.getName().equals("close")) {
                    releaseConnection(physicalConnection);
                    return null; // Do not close the physical connection
                }
                try {
                    return method.invoke(physicalConnection, args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }

    private synchronized void releaseConnection(Connection con) {
        activeConnections.remove(con);
        try {
            if (!con.isClosed()) {
                freeConnections.add(con);
            }
        } catch (SQLException ignore) {}
    }

    public synchronized void shutdown() {
        for (Connection con : freeConnections) {
            try { con.close(); } catch (SQLException ignore) {}
        }
        freeConnections.clear();
        for (Connection con : activeConnections) {
            try { con.close(); } catch (SQLException ignore) {}
        }
        activeConnections.clear();
    }
}
