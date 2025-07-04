package com.cufufy.cufufyCore.module;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Interface for CufufyCore modules (satellite plugins).
 * Allows the core plugin to manage and interact with them in a standardized way.
 */
public interface CufufyModule {

    /**
     * @return The name of this module. Should match the plugin name.
     */
    String getModuleName();

    /**
     * @return The version of this module. Should match the plugin version.
     */
    String getModuleVersion();

    /**
     * Called when the module is specifically enabled by CufufyCore's ModuleManager.
     * This can be used for logic that depends on the Core being ready.
     * For simple modules, their own onEnable might be sufficient if they register late.
     *
     * @param coreInstance The instance of CufufyCore, providing access to core services.
     */
    void onModuleEnable(com.cufufy.cufufyCore.CufufyCore coreInstance);

    /**
     * Called when the module is specifically disabled by CufufyCore's ModuleManager.
     * This can be used for logic that depends on the Core.
     */
    void onModuleDisable();

    /**
     * Generates a string containing diagnostic information for this module.
     * Used by the /cufufycore dump command.
     *
     * @return A string with dump information, can be multi-line.
     */
    String generateDumpInfo();

    /**
     * Provides the JavaPlugin instance of this module.
     * This is useful for the ModuleManager to have a direct reference.
     *
     * @return The JavaPlugin instance of this module.
     */
    JavaPlugin getPluginInstance();

    /**
     * @return The bStats plugin ID for this specific module.
     *         Return 0 or a negative number if this module does not use bStats
     *         or if metrics should be disabled for it.
     */
    int getBstatsPluginId();
}
