package com.cufufy.cufufyCore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.cufufy.cufufyCore.CufufyCore;
import com.cufufy.cufufyCore.module.CufufyModule;
import com.cufufy.cufufyCore.module.ModuleManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.stream.Collectors;

@CommandAlias("cufufycore|ccore|core")
public class CoreCommands extends BaseCommand {

    private final CufufyCore corePlugin;
    private final ModuleManager moduleManager;

    public CoreCommands(CufufyCore corePlugin, ModuleManager moduleManager) {
        this.corePlugin = corePlugin;
        this.moduleManager = moduleManager;

        // Registering command completions for module names
        corePlugin.getCommandManager().getCommandCompletions().registerAsyncCompletion("cufufymodules", c -> {
            return moduleManager.getAllModules().stream()
                    .map(CufufyModule::getModuleName)
                    .collect(Collectors.toList());
        });
    }

    @Subcommand("version")
    @Description("Displays the version of CufufyCore or a specific module.")
    @CommandPermission("cufufycore.command.version")
    @Syntax("[module_name]")
    @CommandCompletion("@cufufymodules")
    public void onVersion(CommandSender sender, @Optional String moduleName) {
        if (moduleName == null || moduleName.equalsIgnoreCase("cufufycore") || moduleName.equalsIgnoreCase(corePlugin.getName())) {
            PluginDescriptionFile desc = corePlugin.getDescription();
            sender.sendMessage(ChatColor.GOLD + desc.getName() + ChatColor.YELLOW + " Version: " + ChatColor.WHITE + desc.getVersion());
            sender.sendMessage(ChatColor.GRAY + "Authors: " + String.join(", ", desc.getAuthors()));
            sender.sendMessage(ChatColor.BLUE + "Website: " + desc.getWebsite());

            String loadedModules = moduleManager.getAllModules().stream()
                    .map(m -> m.getModuleName() + " (v" + m.getModuleVersion() + ")")
                    .collect(Collectors.joining(", "));
            if (loadedModules.isEmpty()) {
                loadedModules = "None";
            }
            sender.sendMessage(ChatColor.GREEN + "Loaded Modules: " + ChatColor.WHITE + loadedModules);

        } else {
            moduleManager.getModule(moduleName).ifPresentOrElse(module -> {
                sender.sendMessage(ChatColor.GOLD + module.getModuleName() + ChatColor.YELLOW + " Version: " + ChatColor.WHITE + module.getModuleVersion());
                if (module.getPluginInstance() != null) {
                    PluginDescriptionFile desc = module.getPluginInstance().getDescription();
                    if (desc.getAuthors() != null && !desc.getAuthors().isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + "Authors: " + String.join(", ", desc.getAuthors()));
                    }
                    if (desc.getWebsite() != null) {
                        sender.sendMessage(ChatColor.BLUE + "Website: " + desc.getWebsite());
                    }
                }
            }, () -> {
                sender.sendMessage(ChatColor.RED + "Module '" + moduleName + "' not found or not registered.");
            });
        }
    }

    @Subcommand("dump")
    @Description("Generates and displays diagnostic information for CufufyCore or a specific module.")
    @CommandPermission("cufufycore.command.dump")
    @Syntax("[module_name]")
    @CommandCompletion("@cufufymodules")
    public void onDump(CommandSender sender, @Optional String moduleName) {
        if (moduleName == null || moduleName.equalsIgnoreCase("cufufycore") || moduleName.equalsIgnoreCase(corePlugin.getName())) {
            sender.sendMessage(ChatColor.GOLD + "--- CufufyCore Dump ---");
            PluginDescriptionFile desc = corePlugin.getDescription();
            sender.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + desc.getName());
            sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + desc.getVersion());
            sender.sendMessage(ChatColor.YELLOW + "API Version: " + ChatColor.WHITE + desc.getAPIVersion());
            sender.sendMessage(ChatColor.YELLOW + "Loaded Modules: " + ChatColor.WHITE +
                    moduleManager.getAllModules().stream().map(CufufyModule::getModuleName).collect(Collectors.joining(", ")));
            // Add more core-specific dump info here if needed
            sender.sendMessage(ChatColor.GOLD + "----------------------");
        } else {
            moduleManager.getModule(moduleName).ifPresentOrElse(module -> {
                sender.sendMessage(ChatColor.GOLD + "--- Dump for " + module.getModuleName() + " ---");
                try {
                    String dumpInfo = module.generateDumpInfo();
                    for (String line : dumpInfo.split("\n")) {
                        sender.sendMessage(ChatColor.WHITE + line);
                    }
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Error generating dump for " + module.getModuleName() + ": " + e.getMessage());
                    corePlugin.getLogger().log(java.util.logging.Level.WARNING, "Error generating dump for " + module.getModuleName(), e);
                }
                sender.sendMessage(ChatColor.GOLD + "----------------------");
            }, () -> {
                sender.sendMessage(ChatColor.RED + "Module '" + moduleName + "' not found or not registered.");
            });
        }
    }

    @HelpCommand
    @Private // Hides it from default /help
    public void onHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "CufufyCore Commands:");
        sender.sendMessage(ChatColor.AQUA + "/ccore version [module_name]" + ChatColor.GRAY + " - Shows version info.");
        sender.sendMessage(ChatColor.AQUA + "/ccore dump [module_name]" + ChatColor.GRAY + " - Shows diagnostic info.");
        // Add other core commands here if any
    }
}
