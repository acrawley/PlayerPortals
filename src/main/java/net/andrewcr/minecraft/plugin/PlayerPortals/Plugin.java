package net.andrewcr.minecraft.plugin.PlayerPortals;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.commands.*;
import net.andrewcr.minecraft.plugin.PlayerPortals.managers.PortalManager;
import net.andrewcr.minecraft.plugin.PlayerPortals.managers.SignManager;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.config.ConfigStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Plugin extends JavaPlugin {
    //region Private Fields

    private Map<String, ICommand> commandMap;
    @Getter
    private PortalStore portalStore;
    @Getter
    private ConfigStore configStore;

    @Getter
    private static Plugin instance;

    //endregion

    @Override
    public void onEnable() {
        // Reset state
        Plugin.instance = this;
        this.commandMap = new HashMap<>();

        // Register commands
        this.registerCommand(new PortalCommand());
        this.registerCommand(new PortalListCommand());
        this.registerCommand(new PortalInfoCommand());
        this.registerCommand(new PortalImportCommand());
        this.registerCommand(new PortalSetCommand());
        this.registerCommand(new PortalTeleportCommand());

        // Register listeners
        this.registerListener(new SignManager());
        this.registerListener(new PortalManager());

        // Configuration
        this.portalStore = new PortalStore();
        this.configStore = new ConfigStore();

        this.portalStore.load();
        this.configStore.load();
    }

    @Override
    public void onDisable() {
        // Save data
        this.portalStore.save();

        // Configuration isn't modified by the plugin, no need to save it

        Plugin.instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        ICommand cmd = this.commandMap.get(cmdName);
        if (cmd == null) {
            this.getLogger().severe("No handler for command '" + cmdName + "'!");
            return false;
        }

        return cmd.invoke(sender, args);
    }

    void registerCommand(ICommand factory) {
        this.commandMap.put(factory.getCommandName().toLowerCase(), factory);
    }

    void registerListener(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }
}
