package com.cufufy.cufufyCore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin corePlugin; // Could be CufufyCore instance

    public ConfigManager(JavaPlugin plugin) {
        this.corePlugin = plugin;
    }

    /**
     * Loads a configuration file for a given module (JavaPlugin).
     * If the file doesn't exist, it attempts to save the default config from the module's JAR.
     *
     * @param moduleInstance The instance of the module (plugin) whose config is being loaded.
     * @param configFileName The name of the configuration file (e.g., "config.yml").
     * @return The loaded FileConfiguration, or null if an error occurs.
     */
    public FileConfiguration loadModuleConfig(JavaPlugin moduleInstance, String configFileName) {
        File configFile = new File(moduleInstance.getDataFolder(), configFileName);

        // Save default config if it doesn't exist
        if (!configFile.exists()) {
            moduleInstance.saveResource(configFileName, false);
            corePlugin.getLogger().info("Saved default config '" + configFileName + "' for module '" + moduleInstance.getName() + "'.");
        }

        // Load the configuration
        try {
            return YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            corePlugin.getLogger().log(Level.SEVERE, "Could not load config '" + configFileName + "' for module '" + moduleInstance.getName() + "'.", e);
            return null;
        }
    }

    /**
     * Reloads a configuration file for a given module.
     *
     * @param moduleInstance The instance of the module.
     * @param configFileName The name of the configuration file.
     * @return The reloaded FileConfiguration, or null if an error occurs.
     */
    public FileConfiguration reloadModuleConfig(JavaPlugin moduleInstance, String configFileName) {
         File configFile = new File(moduleInstance.getDataFolder(), configFileName);
         if (!configFile.exists()) {
             corePlugin.getLogger().warning("Config file '" + configFileName + "' for module '" + moduleInstance.getName() + "' does not exist. Cannot reload.");
             // Optionally save default and load if that's desired behavior for reload on missing file
             // return loadModuleConfig(moduleInstance, configFileName);
             return null;
         }
        try {
            return YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            corePlugin.getLogger().log(Level.SEVERE, "Could not reload config '" + configFileName + "' for module '" + moduleInstance.getName() + "'.", e);
            return null;
        }
    }

    /**
     * Saves a FileConfiguration object to a file for a given module.
     *
     * @param moduleInstance The instance of the module.
     * @param configFileName The name of the configuration file.
     * @param config The FileConfiguration object to save.
     * @return true if successful, false otherwise.
     */
    public boolean saveModuleConfig(JavaPlugin moduleInstance, String configFileName, FileConfiguration config) {
        File configFile = new File(moduleInstance.getDataFolder(), configFileName);
        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            corePlugin.getLogger().log(Level.SEVERE, "Could not save config '" + configFileName + "' for module '" + moduleInstance.getName() + "'.", e);
            return false;
        }
    }

    // Convenience method to get CufufyCore's own config
    public FileConfiguration getCoreConfig() {
        return corePlugin.getConfig();
    }

    public void saveCoreConfig() {
        corePlugin.saveConfig();
    }

    public void reloadCoreConfig() {
        corePlugin.reloadConfig();
    }
}
