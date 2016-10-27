package net.andrewcr.minecraft.plugin.PlayerPortals;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.BasePluginLib.plugin.PluginBase;
import net.andrewcr.minecraft.plugin.PlayerPortals.commands.*;
import net.andrewcr.minecraft.plugin.PlayerPortals.listeners.PortalListener;
import net.andrewcr.minecraft.plugin.PlayerPortals.listeners.SignListener;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.config.ConfigStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;

public class Plugin extends PluginBase {
    //region Private Fields

    @Getter
    private PortalStore portalStore;
    @Getter
    private ConfigStore configStore;

    @Getter
    private static Plugin instance;

    //endregion

    @Override
    public void onEnable() {
        super.onEnable();

        // Reset state
        Plugin.instance = this;

        // Register commands
        this.registerCommand(new PortalCommand());
        this.registerCommand(new PortalListCommand());
        this.registerCommand(new PortalInfoCommand());
        this.registerCommand(new PortalImportCommand());
        this.registerCommand(new PortalSetCommand());
        this.registerCommand(new PortalTeleportCommand());

        // Register listeners
        this.registerListener(new SignListener());
        this.registerListener(new PortalListener());

        // Configuration
        this.portalStore = new PortalStore();
        this.configStore = new ConfigStore();

        this.portalStore.load();
        this.configStore.load();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Save data
        this.portalStore.save();

        // Configuration isn't modified by the plugin, no need to save it

        Plugin.instance = null;
    }
}
