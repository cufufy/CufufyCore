# CufufyCore

## Overview

CufufyCore is a foundational plugin for Minecraft servers running PaperMC (or compatible forks). It serves as a core library and utility provider for other plugins developed under the "Cufufy" umbrella, promoting a modular and consistent development approach. It offers shared services such as command management, configuration handling, database access, and metrics integration.

## Features

*   **Modular Architecture:** Allows other plugins (modules) to register and leverage core functionalities.
*   **Advanced Command Framework:** Utilizes Aikar Command Framework (ACF) for easy and powerful command creation.
*   **Database Service:** Robust database connection pooling using HikariCP, with support for:
    *   MySQL
    *   MariaDB
    *   PostgreSQL
    *   SQLite
*   **Configuration Management:** Utilities for managing `config.yml` for both the core plugin and its modules.
*   **Metrics Service:** Integrated bStats support for CufufyCore itself and for its modules, allowing for usage statistics tracking.

## For Server Administrators

### Installation

1.  **Download:** Obtain the latest `CufufyCore-X.Y.Z.jar` file.
2.  **Server Version:** Ensure your server is running PaperMC or a compatible fork that supports Minecraft version 1.21 or newer (CufufyCore is built against Java 21 and Paper API 1.21).
3.  **Place JAR:** Put the downloaded JAR file into your server's `plugins/` directory.
4.  **Start Server:** Start or restart your server. CufufyCore will generate its default configuration files.

### Configuration (`plugins/CufufyCore/config.yml`)

CufufyCore's main configuration file allows you to set up the database connection and manage metrics.

```yaml
# Main configuration for CufufyCore

# Database settings
database:
  enabled: true # Set to false to disable database features
  type: "sqlite" # Options: "sqlite", "mysql", "mariadb", "postgresql"
  sqlite_file: "database.db" # Relative to CufufyCore's plugin data folder (plugins/CufufyCore/database.db)

  credentials: # Required for mysql, mariadb, postgresql
    host: "localhost"
    port: 3306 # Default for MySQL/MariaDB, 5432 for PostgreSQL
    database: "cufufy_core"
    username: "user"
    password: "password"
    properties: "?autoReconnect=true&useSSL=false" # Optional JDBC connection string properties (e.g., ?useUnicode=true&characterEncoding=utf8)

  pool_settings: # HikariCP connection pool settings
    pool_name: "CufufyCore-HikariPool"
    maximum_pool_size: 10 # Max number of connections in the pool
    minimum_idle: 2       # Min number of idle connections
    max_lifetime_ms: 1800000 # 30 minutes; max lifetime of a connection
    connection_timeout_ms: 30000 # 30 seconds; max wait time for a connection
    idle_timeout_ms: 600000 # 10 minutes; max time an idle connection is kept
    # Optional HikariCP DataSource properties (key: value)
    # E.g., for MySQL to improve performance with prepared statements:
    # properties:
    #   cachePrepStmts: "true"
    #   prepStmtCacheSize: "250"
    #   prepStmtCacheSqlLimit: "2048"
    properties: {}

# Metrics settings (bStats)
metrics:
  # Enables bStats metrics collection for CufufyCore itself.
  # CufufyCore has its own bStats ID.
  # It's highly recommended to keep this enabled to help the developers.
  core_bstats_enabled: true
```

### Commands and Permissions

*   `/cufufycore version [module_name]` (Aliases: `/ccore version`, `/core version`)
    *   **Description:** Displays the version of CufufyCore or a specific registered module.
    *   **Permission:** `cufufycore.command.version`
*   `/cufufycore dump [module_name]` (Aliases: `/ccore dump`, `/core dump`)
    *   **Description:** Generates and displays diagnostic information for CufufyCore or a specific module. Useful for debugging.
    *   **Permission:** `cufufycore.command.dump`

## For Developers (Using CufufyCore as a Dependency)

CufufyCore is designed to be a library for your Cufufy-style plugins (modules).

### Adding CufufyCore as a Maven Dependency

1.  **Add Repository:** Add the GitHub Packages Maven repository to your `pom.xml` (or the repository where CufufyCore is hosted, if different).

    ```xml
    <repositories>
        <repository>
            <id>github</id>
            <name>GitHub cufufy Apache Maven Packages</name>
            <url>https://maven.pkg.github.com/cufufy/CufufyCore</url> <!-- Adjust if your username/repo is different -->
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <!-- Other repositories like PaperMC, etc. -->
    </repositories>
    ```
    *Note: You might need to authenticate with GitHub Packages. Refer to GitHub's documentation.*

2.  **Add Dependency:** Add CufufyCore to your project's dependencies in `pom.xml`.

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.cufufy</groupId>
            <artifactId>cufufycore</artifactId>
            <version>1.0-SNAPSHOT</version> <!-- Replace with the desired CufufyCore version -->
            <scope>provided</scope> <!-- CufufyCore will be on the server -->
        </dependency>
        <!-- Other dependencies -->
    </dependencies>
    ```

### Implementing `CufufyModule`

Your plugin's main class should implement the `CufufyModule` interface. This allows CufufyCore to manage your plugin and provide services to it.

```java
package com.example.mymodule;

import com.cufufy.cufufyCore.CufufyCore;
import com.cufufy.cufufyCore.module.CufufyModule;
import org.bukkit.plugin.java.JavaPlugin;

public class MyModule extends JavaPlugin implements CufufyModule {

    @Override
    public void onEnable() {
        // Standard Bukkit onEnable logic

        // Register this module with CufufyCore
        // It's good practice to ensure CufufyCore is loaded first by adding
        // depend: [CufufyCore] or loadbefore: [CufufyCore] in your plugin.yml
        CufufyCore core = (CufufyCore) getServer().getPluginManager().getPlugin("CufufyCore");
        if (core != null && core.isEnabled()) {
            core.getModuleManager().registerModule(this);
            // Alternative static access:
            // CufufyCore.getCoreModuleManager().registerModule(this);
        } else {
            getLogger().severe("CufufyCore not found or not enabled! MyModule might not function correctly.");
            // Handle core not being available
        }

        getLogger().info(getModuleName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Standard Bukkit onDisable logic
        // ModuleManager will call onModuleDisable automatically if registered.
        getLogger().info(getModuleName() + " has been disabled.");
    }

    // --- CufufyModule Implementation ---

    @Override
    public String getModuleName() {
        return getDescription().getName(); // Typically the plugin's name
    }

    @Override
    public String getModuleVersion() {
        return getDescription().getVersion(); // Typically the plugin's version
    }

    @Override
    public void onModuleEnable(CufufyCore coreInstance) {
        // Logic specific to when CufufyCore enables this module.
        // This is called by ModuleManager.registerModule()
        // coreInstance provides direct access to CufufyCore if needed.
        getLogger().info(getModuleName() + " successfully enabled as a CufufyCore module.");
        // Example: Accessing core services
        // coreInstance.getCommandManager().registerCommand(...);
        // FileConfiguration myConfig = coreInstance.getConfigManager().loadModuleConfig(this, "module_specific_config.yml");
    }

    @Override
    public void onModuleDisable() {
        // Logic specific to when CufufyCore disables this module.
        // This is called by ModuleManager.unregisterModule() or during CufufyCore's shutdown.
        getLogger().info(getModuleName() + " is being disabled as a CufufyCore module.");
    }

    @Override
    public String generateDumpInfo() {
        // Provide diagnostic information specific to your module.
        // This is called by the /ccore dump <your_module_name> command.
        StringBuilder sb = new StringBuilder();
        sb.append("MyModule Specific Info:\n");
        sb.append(" - Status: Operational\n");
        sb.append(" - Custom Setting X: some_value\n");
        return sb.toString();
    }

    @Override
    public JavaPlugin getPluginInstance() {
        return this; // Return the instance of your plugin's main class
    }

    @Override
    public int getBstatsPluginId() {
        return 12345; // Replace with YOUR module's bStats Plugin ID, or 0 if none.
    }
}
```

And in your `plugin.yml`:

```yaml
name: MyModule
version: 1.0.0
main: com.example.mymodule.MyModule
api-version: '1.21'
depend: [CufufyCore] # Important: Ensures CufufyCore loads before your module
authors: [YourName]
description: An example module using CufufyCore.
```

### Accessing Core Services

CufufyCore provides static accessor methods for its services:

*   **ModuleManager:** Manage module registration.
    ```java
    com.cufufy.cufufyCore.module.ModuleManager moduleManager = CufufyCore.getCoreModuleManager();
    moduleManager.registerModule(this);
    ```
*   **CommandManager (ACF):** Register commands for your module.
    ```java
    co.aikar.commands.PaperCommandManager commandManager = CufufyCore.getCoreCommandManager();
    commandManager.registerCommand(new MyModuleCommands(this));
    ```
*   **DatabaseService:** Obtain database connections.
    ```java
    com.cufufy.cufufyCore.database.DatabaseService dbService = CufufyCore.getCoreDatabaseService();
    if (dbService.isEnabled()) {
        try (java.sql.Connection conn = dbService.getConnection()) {
            // Use the connection
        } catch (java.sql.SQLException e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Database error", e);
        }
    }
    ```
*   **ConfigManager:** Load and save configurations for your module.
    ```java
    com.cufufy.cufufyCore.config.ConfigManager configManager = CufufyCore.getCoreConfigManager();
    org.bukkit.configuration.file.FileConfiguration myCustomConfig = configManager.loadModuleConfig(this, "my_custom_config.yml");
    if (myCustomConfig != null) {
        // Use myCustomConfig
        // configManager.saveModuleConfig(this, "my_custom_config.yml", myCustomConfig);
    }
    ```
*   **MetricsService:** CufufyCore automatically handles bStats registration for your module if you provide a valid `bStatsPluginId` in your `CufufyModule` implementation. You can also access the service if needed for more advanced custom charts, though typically direct interaction isn't required for basic metrics.
    ```java
    // com.cufufy.cufufyCore.metrics.MetricsService metricsService = CufufyCore.getCoreMetricsService();
    // For most uses, just implementing getBstatsPluginId() in CufufyModule is enough.
    ```

### Developing Modules with ACF Commands

CufufyCore utilizes the Aikar Command Framework (ACF) for command handling and provides access to its command manager for modules.

**Important: ACF Shading and Annotation Usage**

CufufyCore embeds (shades) ACF directly into its JAR. During this process, ACF's packages are relocated to prevent conflicts with other plugins. Specifically:

- The standard `co.aikar.commands` package is relocated to `com.cufufy.cufufycore.lib.acf`.
- The standard `co.aikar.locales` package is relocated to `com.cufufy.cufufycore.lib.locales`.

When creating command classes in your module that extend `com.cufufy.cufufycore.lib.acf.BaseCommand` (which is the shaded version of ACF's `BaseCommand`), you **must** import ACF annotations from the shaded package path.

**Example of correct annotation imports in your module's command class:**

```java
// In your module's command class (e.g., MyModuleCommand.java)
package com.myplugin.mymodule.commands;

import com.cufufy.cufufycore.lib.acf.BaseCommand; // Correct BaseCommand
import com.cufufy.cufufycore.lib.acf.annotation.*; // Correct import for ALL ACF annotations
// import com.cufufy.cufufycore.lib.acf.annotation.CommandAlias; // Or import specific annotations
// import com.cufufy.cufufycore.lib.acf.annotation.Default;
// import com.cufufy.cufufycore.lib.acf.annotation.Subcommand;

import org.bukkit.command.CommandSender;

@CommandAlias("mymodulecmd")
public class MyModuleCommand extends BaseCommand {

    @Default
    public void onDefault(CommandSender sender) {
        sender.sendMessage("MyModule default command executed!");
    }
    
    @Subcommand("hello")
    public void onHello(CommandSender sender) {
        sender.sendMessage("Hello from MyModule!");
    }
}
```

Using incorrect imports (e.g., `import co.aikar.commands.annotation.*;`) will likely result in your module's commands not being registered or recognized by CufufyCore, even if your module compiles successfully.

**Registering Commands from Your Module:**

In your module's `onModuleEnable(CufufyCore coreInstance)` method, you can get the command manager and register your commands like so:

```java
// Note: The type PaperCommandManager should also be from the shaded package if you declare it explicitly
// com.cufufy.cufufycore.lib.acf.PaperCommandManager commandManager = coreInstance.getCoreCommandManager(); 
// However, CufufyCore.getCoreCommandManager() already returns the correct type.
var commandManager = coreInstance.getCoreCommandManager(); // Using var for simplicity
commandManager.registerCommand(new MyModuleCommand(/* any dependencies */)); 
```

### Example Usage Snippets

**Registering your plugin as a module (in your plugin's `onEnable`):**

```java
// In your module's main class (which implements CufufyModule)
@Override
public void onEnable() {
    // ... other onEnable logic ...

    // Ensure CufufyCore is available and then register
    CufufyCore coreApi = (CufufyCore) getServer().getPluginManager().getPlugin("CufufyCore");
    if (coreApi != null && coreApi.isEnabled()) {
        CufufyCore.getCoreModuleManager().registerModule(this);
        getLogger().info(getModuleName() + " registered with CufufyCore.");
    } else {
        getLogger().severe("CufufyCore not found or disabled. " + getModuleName() + " may not function correctly.");
        // Consider disabling your plugin or parts of it if CufufyCore is critical
        // getServer().getPluginManager().disablePlugin(this);
        // return;
    }

    // ... rest of your onEnable logic ...
}
```
*Make sure to add `depend: [CufufyCore]` to your `plugin.yml`.*

**Registering a command for your module:**
*(This is now better covered in the "Developing Modules with ACF Commands" section)*

**Accessing the database:**

```java
// Example method in your module
public void savePlayerData(java.util.UUID playerId, String data) {
    com.cufufy.cufufyCore.database.DatabaseService dbService = CufufyCore.getCoreDatabaseService();
    if (!dbService.isEnabled()) {
        getLogger().warning("Database service not enabled. Cannot save player data.");
        return;
    }

    String sql = "INSERT INTO player_data (uuid, some_data) VALUES (?, ?) ON DUPLICATE KEY UPDATE some_data = ?;";
    try (java.sql.Connection conn = dbService.getConnection();
         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, playerId.toString());
        pstmt.setString(2, data);
        pstmt.setString(3, data);
        pstmt.executeUpdate();
    } catch (java.sql.SQLException e) {
        getLogger().log(java.util.logging.Level.SEVERE, "Could not save player data for " + playerId, e);
    }
}
```

## Building from Source

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/cufufy/CufufyCore.git # Or your repository URL
    cd CufufyCore
    ```
2.  **Build with Maven:**
    ```bash
    mvn clean package
    ```
    The compiled JAR will be in the `target/` directory.

## License

This project is currently not distributed under a specific license. All rights reserved.
Please contact the author for permissions regarding use or distribution.
