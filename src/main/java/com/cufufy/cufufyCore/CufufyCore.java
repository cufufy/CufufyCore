package com.cufufy.cufufyCore;

import co.aikar.commands.PaperCommandManager;
import com.cufufy.cufufyCore.module.ModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CufufyCore extends JavaPlugin {

    private static CufufyCore instance;
    private ModuleManager moduleManager;
    private PaperCommandManager commandManager;
    private com.cufufy.cufufyCore.database.DatabaseService databaseService; // FQDN to avoid import clash if any
    private com.cufufy.cufufyCore.config.ConfigManager configManager;
    private com.cufufy.cufufyCore.metrics.MetricsService metricsService;

    @Override
    public void onLoad() {
        instance = this; // Initialize instance early for static access if needed by modules during their onLoad
    }

    @Override
    public void onEnable() {
        // Initialize ModuleManager
        this.moduleManager = new ModuleManager(this);

        // Initialize ACF PaperCommandManager
        this.commandManager = new PaperCommandManager(this);
        // Configure ACF (e.g., command completions, contexts, locales) if needed globally
        // For now, basic initialization is sufficient. Modules can add their own.

        // Register CufufyCore's own commands
        this.commandManager.registerCommand(new com.cufufy.cufufyCore.commands.CoreCommands(this, this.moduleManager));
        getLogger().info("ACF PaperCommandManager initialized and core commands registered.");

        // Initialize DatabaseService
        // This needs to be after saveDefaultConfig to ensure config.yml is available
        saveDefaultConfig(); // Ensures config.yml exists with defaults if not present
        this.databaseService = new com.cufufy.cufufyCore.database.DatabaseService(this);
        if (this.databaseService.isEnabled()) {
            getLogger().info("DatabaseService initialized.");
        } else {
            getLogger().warning("DatabaseService is not enabled or failed to initialize. Check config.yml and logs.");
        }

        // Initialize ConfigManager
        this.configManager = new com.cufufy.cufufyCore.config.ConfigManager(this);
        getLogger().info("ConfigManager initialized.");

        // Initialize MetricsService
        // This should be after config is loaded if MetricsService depends on config values (which it does)
        this.metricsService = new com.cufufy.cufufyCore.metrics.MetricsService(this);
        // MetricsService logs its own status


        getLogger().info("CufufyCore has been enabled.");

        // Modules will register themselves by calling CufufyCore.getCoreModuleManager().registerModule(thisModuleInstance);
        // and CufufyCore.getCoreCommandManager().registerCommand(...) from their onEnable method.
        // They can also access CufufyCore.getCoreDatabaseService() if needed.
    }

    @Override
    public void onDisable() {
        // Close DatabaseService connection pool
        if (this.databaseService != null && this.databaseService.isEnabled()) {
            this.databaseService.close();
        }

        // Unregister commands if necessary (ACF handles much of this)
        // if (this.commandManager != null) {
            // commandManager.unregisterCommands();
        // }

        // Disable all registered modules
        if (this.moduleManager != null) {
            this.moduleManager.disableAllModules();
        }

        getLogger().info("CufufyCore has been disabled.");
    }

    /**
     * Gets the singleton instance of the CufufyCore plugin.
     *
     * @return The CufufyCore instance.
     * @throws IllegalStateException if the plugin is not loaded yet.
     */
    public static CufufyCore getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CufufyCore has not been loaded yet or is disabled.");
        }
        return instance;
    }

    /**
     * Gets the ModuleManager instance.
     * Modules will use this to register themselves.
     *
     * @return The ModuleManager instance.
     * @throws IllegalStateException if the plugin is not enabled yet (ModuleManager is initialized in onEnable).
     */
    public ModuleManager getModuleManager() {
        if (moduleManager == null) {
            // This might happen if a module tries to access it too early (e.g. in its constructor or onLoad)
            // before CufufyCore's onEnable has run.
            // Proper dependency management (depend in plugin.yml) should prevent this for onEnable calls.
            throw new IllegalStateException("ModuleManager is not available yet. CufufyCore might not be fully enabled.");
        }
        return moduleManager;
    }

    // Static accessor for modules to easily get the ModuleManager
    public static ModuleManager getCoreModuleManager() {
        return getInstance().getModuleManager();
    }

    /**
     * Gets the PaperCommandManager instance for registering commands.
     *
     * @return The PaperCommandManager instance.
     * @throws IllegalStateException if the plugin or command manager is not initialized yet.
     */
    public PaperCommandManager getCommandManager() {
        if (commandManager == null) {
            throw new IllegalStateException("PaperCommandManager is not available yet. CufufyCore might not be fully enabled.");
        }
        return commandManager;
    }

    // Static accessor for modules to easily get the PaperCommandManager
    public static PaperCommandManager getCoreCommandManager() {
        return getInstance().getCommandManager();
    }

    /**
     * Gets the DatabaseService instance.
     *
     * @return The DatabaseService instance.
     * @throws IllegalStateException if the plugin or database service is not initialized yet.
     */
    public com.cufufy.cufufyCore.database.DatabaseService getDatabaseService() {
        if (databaseService == null) {
            throw new IllegalStateException("DatabaseService is not available yet. CufufyCore might not be fully enabled or service failed to initialize.");
        }
        return databaseService;
    }

    // Static accessor for modules to easily get the DatabaseService
    public static com.cufufy.cufufyCore.database.DatabaseService getCoreDatabaseService() {
        return getInstance().getDatabaseService();
    }

    /**
     * Gets the ConfigManager instance.
     *
     * @return The ConfigManager instance.
     * @throws IllegalStateException if the plugin or config manager is not initialized yet.
     */
    public com.cufufy.cufufyCore.config.ConfigManager getConfigManager() {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager is not available yet. CufufyCore might not be fully enabled.");
        }
        return configManager;
    }

    // Static accessor for modules to easily get the ConfigManager
    public static com.cufufy.cufufyCore.config.ConfigManager getCoreConfigManager() {
        return getInstance().getConfigManager();
    }

    /**
     * Gets the MetricsService instance.
     * Modules can use this to register their own bStats ID.
     *
     * @return The MetricsService instance.
     * @throws IllegalStateException if the plugin or metrics service is not initialized yet.
     */
    public com.cufufy.cufufyCore.metrics.MetricsService getMetricsService() {
        if (metricsService == null) {
            throw new IllegalStateException("MetricsService is not available yet. CufufyCore might not be fully enabled.");
        }
        return metricsService;
    }

    // Static accessor for modules to easily get the MetricsService
    public static com.cufufy.cufufyCore.metrics.MetricsService getCoreMetricsService() {
        return getInstance().getMetricsService();
    }
}
