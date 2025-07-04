package com.cufufy.cufufyCore.module;

import com.cufufy.cufufyCore.CufufyCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List; // Added import
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class ModuleManager {

    private final CufufyCore corePlugin;
    private final Map<String, CufufyModule> modules = new LinkedHashMap<>();

    public ModuleManager(CufufyCore corePlugin) {
        this.corePlugin = corePlugin;
    }

    public void registerModule(CufufyModule module) {
        if (module == null || module.getPluginInstance() == null) {
            corePlugin.getLogger().warning("Attempted to register a null module or module with null plugin instance.");
            return;
        }
        String moduleName = module.getModuleName();
        if (modules.containsKey(moduleName.toLowerCase())) {
            corePlugin.getLogger().warning("Module '" + moduleName + "' is already registered. Ignoring duplicate registration.");
            return;
        }
        try {
            // Pass the CufufyCore instance directly
            module.onModuleEnable((CufufyCore) corePlugin);
            modules.put(moduleName.toLowerCase(), module);
            corePlugin.getLogger().info("Registered and enabled module: " + moduleName + " v" + module.getModuleVersion());

            // Register metrics for the module
            if (corePlugin.getMetricsService() != null) { // Ensure MetricsService is initialized
                corePlugin.getMetricsService().registerModuleMetrics(module);
            } else {
                corePlugin.getLogger().warning("[Metrics] MetricsService not available when registering module: " + moduleName);
            }

        } catch (Exception e) {
            corePlugin.getLogger().log(Level.SEVERE, "Error enabling module: " + moduleName, e);
            // Optionally, unregister or mark as failed if onModuleEnable throws an exception
            if (corePlugin.getMetricsService() != null) {
                corePlugin.getMetricsService().unregisterModuleMetrics(module); // Attempt to clean up metrics if module failed to enable
            }
        }
    }

    public void unregisterModule(CufufyModule module) {
        if (module == null || module.getPluginInstance() == null) {
            corePlugin.getLogger().warning("Attempted to unregister a null module or module with null plugin instance.");
            return;
        }
        String moduleName = module.getModuleName();
        if (modules.containsKey(moduleName.toLowerCase())) {
            try {
                module.onModuleDisable();
                // Unregister metrics for the module
                if (corePlugin.getMetricsService() != null) {
                    corePlugin.getMetricsService().unregisterModuleMetrics(module);
                }
                modules.remove(moduleName.toLowerCase());
                corePlugin.getLogger().info("Unregistered and disabled module: " + moduleName);
            } catch (Exception e) {
                corePlugin.getLogger().log(Level.SEVERE, "Error disabling module: " + moduleName, e);
            }
        } else {
            corePlugin.getLogger().warning("Attempted to unregister module '" + moduleName + "' which was not registered.");
        }
    }

    public void disableAllModules() {
        // Iterate a copy of values to avoid ConcurrentModificationException if unregisterModule modifies the map
        for (CufufyModule module : List.copyOf(modules.values())) {
            unregisterModule(module);
        }
        modules.clear(); // Ensure all are cleared even if errors occurred
        corePlugin.getLogger().info("All registered modules have been disabled.");
    }

    public Optional<CufufyModule> getModule(String moduleName) {
        if (moduleName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modules.get(moduleName.toLowerCase()));
    }

    public Collection<CufufyModule> getAllModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public boolean isModuleRegistered(String moduleName) {
        if (moduleName == null) {
            return false;
        }
        return modules.containsKey(moduleName.toLowerCase());
    }
}
