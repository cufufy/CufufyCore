package com.cufufy.cufufyCore.database;

import com.cufufy.cufufyCore.CufufyCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseService {

    private final CufufyCore corePlugin;
    private HikariDataSource dataSource;
    private boolean enabled = false;

    public DatabaseService(CufufyCore corePlugin) {
        this.corePlugin = corePlugin;
        loadConfigAndInitialize();
    }

    private void loadConfigAndInitialize() {
        corePlugin.saveDefaultConfig(); // Ensure config.yml exists
        FileConfiguration config = corePlugin.getConfig();

        if (!config.getBoolean("database.enabled", false)) {
            corePlugin.getLogger().info("Database service is disabled in config.yml. Skipping initialization.");
            this.enabled = false;
            return;
        }

        HikariConfig hikariConfig = new HikariConfig();
        String type = config.getString("database.type", "sqlite").toLowerCase();

        switch (type) {
            case "mysql":
            case "mariadb":
                hikariConfig.setJdbcUrl(String.format("jdbc:%s://%s:%d/%s%s",
                        type.equals("mysql") ? "mysql" : "mariadb", // JDBC driver type correction for MariaDB
                        config.getString("database.credentials.host", "localhost"),
                        config.getInt("database.credentials.port", 3306),
                        config.getString("database.credentials.database", "cufufy"),
                        config.getString("database.credentials.properties", ""))); // e.g. ?autoReconnect=true&useSSL=false
                hikariConfig.setUsername(config.getString("database.credentials.username", "user"));
                hikariConfig.setPassword(config.getString("database.credentials.password", "password"));
                hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver"); // For MySQL 8+ & MariaDB
                break;
            case "postgresql":
                 hikariConfig.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s%s",
                        config.getString("database.credentials.host", "localhost"),
                        config.getInt("database.credentials.port", 5432),
                        config.getString("database.credentials.database", "cufufy"),
                        config.getString("database.credentials.properties", "")));
                hikariConfig.setUsername(config.getString("database.credentials.username", "user"));
                hikariConfig.setPassword(config.getString("database.credentials.password", "password"));
                hikariConfig.setDriverClassName("org.postgresql.Driver");
                break;
            case "sqlite":
            default:
                // SQLite specific setup
                String dbPath = config.getString("database.sqlite_file", "database.db");
                hikariConfig.setJdbcUrl("jdbc:sqlite:" + corePlugin.getDataFolder().getAbsolutePath() + "/" + dbPath);
                // No username/password for SQLite typically needed
                // Driver is usually auto-detected or can be specified if issues arise: org.sqlite.JDBC
                break;
        }

        // Common HikariCP settings
        hikariConfig.setPoolName(config.getString("database.pool_settings.pool_name", "CufufyCore-HikariPool"));
        hikariConfig.setMaximumPoolSize(config.getInt("database.pool_settings.maximum_pool_size", 10));
        hikariConfig.setMinimumIdle(config.getInt("database.pool_settings.minimum_idle", 2));
        hikariConfig.setMaxLifetime(config.getInt("database.pool_settings.max_lifetime_ms", 1800000)); // 30 minutes
        hikariConfig.setConnectionTimeout(config.getInt("database.pool_settings.connection_timeout_ms", 30000)); // 30 seconds
        hikariConfig.setIdleTimeout(config.getInt("database.pool_settings.idle_timeout_ms", 600000)); // 10 minutes

        // Custom properties from config
        if (config.isConfigurationSection("database.pool_settings.properties")) {
            config.getConfigurationSection("database.pool_settings.properties").getKeys(false).forEach(key -> {
                hikariConfig.addDataSourceProperty(key, config.getString("database.pool_settings.properties." + key));
            });
        }


        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            this.enabled = true;
            corePlugin.getLogger().info("Database service initialized successfully for type: " + type);
        } catch (Exception e) {
            corePlugin.getLogger().log(Level.SEVERE, "Failed to initialize database service for type: " + type, e);
            this.dataSource = null;
            this.enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled && this.dataSource != null;
    }

    public Connection getConnection() throws SQLException {
        if (!isEnabled()) {
            throw new SQLException("Database service is not enabled or failed to initialize.");
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            corePlugin.getLogger().log(Level.SEVERE, "Failed to get database connection from pool.", e);
            throw e; // Re-throw to allow callers to handle
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            corePlugin.getLogger().info("Database service connection pool closed.");
        }
        this.enabled = false;
    }
}
