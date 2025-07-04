package com.cufufy.cufufyCore.metrics;

import com.cufufy.cufufyCore.CufufyCore;
import com.cufufy.cufufyCore.module.CufufyModule;
// Use the original bStats package path for compilation
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MetricsService {

    private final CufufyCore corePlugin;
    private final Map<String, Metrics> moduleMetricsMap = new HashMap<>();
    private final boolean coreMetricsEnabled; // Control whether core itself sends metrics

    // bStats Service ID for CufufyCore itself.
    private static final int CUFUFY_CORE_BSTATS_ID = 26370; // Updated with user-provided ID

    public MetricsService(CufufyCore corePlugin) {
        this.corePlugin = corePlugin;
        // Ensure config is loaded before trying to access it
        corePlugin.saveDefaultConfig();
        this.coreMetricsEnabled = corePlugin.getConfig().getBoolean("metrics.core_bstats_enabled", true);

        if (this.coreMetricsEnabled) {
            if (CUFUFY_CORE_BSTATS_ID <= 0) { // Basic check for placeholder
                corePlugin.getLogger().warning("[Metrics] CufufyCore bStats ID (" + CUFUFY_CORE_BSTATS_ID + ") is not set or is invalid. Core metrics will be disabled. Please register the plugin on bStats.org and update the ID in MetricsService.java.");
            } else {
                try {
                    Metrics coreMetrics = new Metrics(corePlugin, CUFUFY_CORE_BSTATS_ID);
                    // TODO: Add any custom charts for CufufyCore itself here
                    // coreMetrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "value"));
                    corePlugin.getLogger().info("[Metrics] bStats metrics enabled for CufufyCore (ID: " + CUFUFY_CORE_BSTATS_ID + ")");
                } catch (Exception e) {
                    corePlugin.getLogger().log(Level.WARNING, "[Metrics] Failed to initialize bStats for CufufyCore. Error: " + e.getMessage(), e);
                }
            }
        } else {
            corePlugin.getLogger().info("[Metrics] bStats for CufufyCore itself is disabled via config.yml (metrics.core_bstats_enabled=false).");
        }
    }

    /**
     * Registers a module with the MetricsService, enabling bStats for it if configured.
     *
     * @param module The CufufyModule instance.
     */
    public void registerModuleMetrics(CufufyModule module) {
        if (module == null) {
            corePlugin.getLogger().warning("[Metrics] Attempted to register metrics for a null module reference.");
            return;
        }
        if (module.getPluginInstance() == null) {
            corePlugin.getLogger().warning("[Metrics] Attempted to register metrics for module '" + module.getModuleName() + "' but its JavaPlugin instance is null.");
            return;
        }

        JavaPlugin modulePlugin = module.getPluginInstance();
        String moduleName = module.getModuleName();
        int bStatsPluginId = module.getBstatsPluginId(); // Assumes this method is added to CufufyModule

        if (moduleMetricsMap.containsKey(moduleName.toLowerCase())) {
            corePlugin.getLogger().warning("[Metrics] Module '" + moduleName + "' already has metrics registered. Skipping.");
            return;
        }

        // Optional: Add a global switch for module metrics if desired in the future
        // boolean moduleMetricsAllowed = corePlugin.getConfig().getBoolean("metrics.modules_enabled", true);
        // if (!moduleMetricsAllowed) {
        //     corePlugin.getLogger().info("[Metrics] Module metrics are globally disabled. Skipping for " + moduleName);
        //     return;
        // }

        if (bStatsPluginId <= 0) {
             corePlugin.getLogger().warning("[Metrics] Invalid bStats ID (" + bStatsPluginId + ") provided by module '" + moduleName + "'. Metrics will not be enabled for it. Please ensure the module returns a valid ID from bStats.org.");
            return;
        }

        try {
            Metrics metrics = new Metrics(modulePlugin, bStatsPluginId);
            // Modules can add their own custom charts using the 'metrics' instance they would typically create
            // within their own plugin's onEnable or specific metric setup methods.
            // This service primarily handles the instantiation with the correct plugin ID.
            moduleMetricsMap.put(moduleName.toLowerCase(), metrics);
            corePlugin.getLogger().info("[Metrics] bStats metrics enabled for module: " + moduleName + " (ID: " + bStatsPluginId + ")");
        } catch (Exception e) {
            corePlugin.getLogger().log(Level.WARNING, "[Metrics] Failed to initialize bStats for module '" + moduleName + "' (ID: " + bStatsPluginId + "). Error: " + e.getMessage(), e);
        }
    }

    public void unregisterModuleMetrics(CufufyModule module) {
        if (module == null) return;
        String moduleName = module.getModuleName();
        if (moduleMetricsMap.containsKey(moduleName.toLowerCase())) {
            moduleMetricsMap.remove(moduleName.toLowerCase());
            corePlugin.getLogger().info("[Metrics] bStats tracking for module '" + moduleName + "' removed from MetricsService map.");
        }
    }
}
