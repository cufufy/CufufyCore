package com.cufufy.cufufyCore;

import co.aikar.commands.PaperCommandManager;
import com.cufufy.cufufyCore.module.ModuleManager;
import com.cufufy.cufufyCore.metrics.MetricsService; // Added import for MetricsService
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class CufufyCore extends JavaPlugin {

    private static CufufyCore instance;
    private ModuleManager moduleManager;
    private PaperCommandManager commandManager;
    private com.cufufy.cufufyCore.database.DatabaseService databaseService; // FQDN to avoid import clash if any
    private com.cufufy.cufufyCore.config.ConfigManager configManager;
    private MetricsService metricsService; // Added MetricsService instance

    @Override
    public void onLoad() {
        instance = this; // Initialize instance early for static access if needed by modules during their onLoad
    }

    @Override
    public void onEnable() {
        // Initialize ModuleManager
        this.moduleManager = new ModuleManager(this);

        // Initialize ACF PaperCommandManager
        getLogger().info("Initializing PaperCommandManager...");
        this.commandManager = new PaperCommandManager(this);
        if (this.commandManager != null) {
            getLogger().info("PaperCommandManager initialized successfully. Class: " + this.commandManager.getClass().getName() + ", Instance HashCode: " + this.commandManager.hashCode());

            // Set logger level for the relocated ACF package.
            java.util.logging.Logger.getLogger("com.cufufy.cufufycore.lib.acf").setLevel(Level.FINER);
            getLogger().info("Verbose logging for 'com.cufufy.cufufycore.lib.acf' configured to FINER.");
        } else {
            getLogger().severe("PaperCommandManager initialization FAILED!");
        }
        // Configure ACF (e.g., command completions, contexts, locales) if needed globally
        // For now, basic initialization is sufficient. Modules can add their own.

        // Register CufufyCore's own commands
        getLogger().info("Registering CufufyCore's own commands...");
        try {
            this.commandManager.registerCommand(new com.cufufy.cufufyCore.commands.CoreCommands(this, this.moduleManager));
            getLogger().info("CufufyCore's own commands registered successfully.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error registering CufufyCore's own commands", e);
        }
        // getLogger().info("ACF PaperCommandManager initialized and core commands registered."); // Original log, now covered by detailed ones

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
        // This should be after config is loaded as it checks config for enabling core metrics
        this.metricsService = new MetricsService(this);
        getLogger().info("MetricsService initialized.");


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
        getLogger().info("CufufyCore.getCommandManager() called.");
        if (commandManager == null) {
            getLogger().warning("CufufyCore.getCommandManager() called, but commandManager is null!");
            throw new IllegalStateException("PaperCommandManager is not available yet. CufufyCore might not be fully enabled.");
        }
        getLogger().info("Returning commandManager instance: " + commandManager.hashCode());
        return commandManager;
    }

    // Static accessor for modules to easily get the PaperCommandManager
    public static PaperCommandManager getCoreCommandManager() {
        getInstance().getLogger().info("CufufyCore.getCoreCommandManager() (static) called.");
        PaperCommandManager manager = getInstance().getCommandManager();
        if (manager != null) {
            getInstance().getLogger().info("CufufyCore.getCoreCommandManager() (static) returning manager instance: " + manager.hashCode());
        } else {
            getInstance().getLogger().warning("CufufyCore.getCoreCommandManager() (static) but getCommandManager() returned null!");
        }
        return manager;
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
     *
     * @return The MetricsService instance.
     * @throws IllegalStateException if the plugin or metrics service is not initialized yet.
     */
    public MetricsService getMetricsService() {
        if (metricsService == null) {
            throw new IllegalStateException("MetricsService is not available yet. CufufyCore might not be fully enabled.");
        }
        return metricsService;
    }

    // Static accessor for modules to easily get the MetricsService
    public static MetricsService getCoreMetricsService() {
        return getInstance().getMetricsService();
    }
}
