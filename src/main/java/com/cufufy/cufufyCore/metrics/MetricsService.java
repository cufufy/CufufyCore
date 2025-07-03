package com.cufufy.cufufyCore.metrics;

import com.cufufy.cufufyCore.CufufyCore;
import com.cufufy.cufufyCore.module.CufufyModule;
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
    // TODO: User needs to register CufufyCore on bStats.org to get this ID.
    private static final int CUFUFY_CORE_BSTATS_ID = 22260; // Placeholder - REPLACE THIS

    public MetricsService(CufufyCore corePlugin) {
        this.corePlugin = corePlugin;
        this.coreMetricsEnabled = corePlugin.getConfig().getBoolean("metrics.core_bstats_enabled", true);

        if (this.coreMetricsEnabled) {
            if (CUFUFY_CORE_BSTATS_ID <= 0) { // Basic check for placeholder
                corePlugin.getLogger().warning("[Metrics] CufufyCore bStats ID is not set or is invalid. Core metrics will be disabled. Please register the plugin on bStats.org.");
            } else {
                try {
                    Metrics coreMetrics = new Metrics(corePlugin, CUFUFY_CORE_BSTATS_ID);
                    // TODO: Add any custom charts for CufufyCore itself here
                    // coreMetrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "value"));
                    corePlugin.getLogger().info("[Metrics] bStats metrics enabled for CufufyCore (ID: " + CUFUFY_CORE_BSTATS_ID + ")");
                } catch (Exception e) {
                    corePlugin.getLogger().log(Level.WARNING, "[Metrics] Failed to initialize bStats for CufufyCore.", e);
                }
            }
        } else {
            corePlugin.getLogger().info("[Metrics] bStats for CufufyCore itself is disabled in config.yml.");
        }
    }

    /**
     * Registers a module with the MetricsService, enabling bStats for it if configured.
     *
     * @param module The CufufyModule instance (which is also a JavaPlugin).
     * @param bStatsPluginId The bStats plugin ID for this specific module.
     */
    public void registerModuleMetrics(CufufyModule module, int bStatsPluginId) {
        if (module == null || module.getPluginInstance() == null) {
            corePlugin.getLogger().warning("[Metrics] Attempted to register metrics for a null module.");
            return;
        }

        JavaPlugin modulePlugin = module.getPluginInstance();
        String moduleName = module.getModuleName();

        if (moduleMetricsMap.containsKey(moduleName.toLowerCase())) {
            corePlugin.getLogger().warning("[Metrics] Module '" + moduleName + "' already has metrics registered. Skipping.");
            return;
        }

        // Check a hypothetical global disable switch or per-module config if needed
        // For now, assume if a module calls this, it wants metrics.
        // boolean moduleMetricsAllowed = corePlugin.getConfig().getBoolean("metrics.allow_module_metrics", true);
        // if (!moduleMetricsAllowed) {
        //     corePlugin.getLogger().info("[Metrics] Module metrics are globally disabled. Skipping for " + moduleName);
        //     return;
        // }

        if (bStatsPluginId <= 0) {
             corePlugin.getLogger().warning("[Metrics] Invalid bStats ID (" + bStatsPluginId + ") provided for module '" + moduleName + "'. Metrics will not be enabled for it.");
            return;
        }

        try {
            Metrics metrics = new Metrics(modulePlugin, bStatsPluginId);
            // Modules can add their own custom charts using the 'metrics' instance they would typically create.
            // This service just centralizes the instantiation if desired, or ensures it happens.
            // If modules handle their own bStats, they don't need to call this.
            moduleMetricsMap.put(moduleName.toLowerCase(), metrics);
            corePlugin.getLogger().info("[Metrics] bStats metrics enabled for module: " + moduleName + " (ID: " + bStatsPluginId + ")");
        } catch (Exception e) {
            corePlugin.getLogger().log(Level.WARNING, "[Metrics] Failed to initialize bStats for module '" + moduleName + "'.", e);
        }
    }

    // In case a module is disabled/unloaded, we might want to clear its metrics instance.
    // However, bStats instances are typically lightweight and manage their own lifecycle
    // with respect to the plugin they are attached to. Explicit removal might not be necessary.
    public void unregisterModuleMetrics(CufufyModule module) {
        if (module == null) return;
        String moduleName = module.getModuleName();
        if (moduleMetricsMap.containsKey(moduleName.toLowerCase())) {
            // bStats doesn't have a public "shutdown" or "unregister" method for a Metrics instance.
            // It ties into the plugin's lifecycle. So, removing from map is mostly for our tracking.
            moduleMetricsMap.remove(moduleName.toLowerCase());
            corePlugin.getLogger().info("[Metrics] bStats tracking for module '" + moduleName + "' removed from MetricsService.");
        }
    }
}
