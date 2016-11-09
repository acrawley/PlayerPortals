package net.andrewcr.minecraft.plugin.PlayerPortals;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.BasePluginLib.plugin.PluginBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.Version;
import net.andrewcr.minecraft.plugin.PlayerPortals.commands.*;
import net.andrewcr.minecraft.plugin.PlayerPortals.integration.distributedspawns.DistributedSpawnsIntegration;
import net.andrewcr.minecraft.plugin.PlayerPortals.integration.dynmap.DynmapIntegration;
import net.andrewcr.minecraft.plugin.PlayerPortals.listeners.PortalListener;
import net.andrewcr.minecraft.plugin.PlayerPortals.listeners.SignListener;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.config.ConfigStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;

public class Plugin extends PluginBase {
    //region Private Fields

    @Getter private static Plugin instance;
    @Getter private PortalStore portalStore;
    @Getter private ConfigStore configStore;
    @Getter private DistributedSpawnsIntegration distributedSpawnsIntegration;
    @Getter private DynmapIntegration dynmapIntegration;

    //endregion

    @Override
    protected Version getRequiredBPLVersion() {
        return new Version(1, 2);
    }

    @Override
    public void onEnableCore() {
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

        // Integration with other plugins
        this.distributedSpawnsIntegration = new DistributedSpawnsIntegration();
        this.dynmapIntegration = new DynmapIntegration();
    }

    @Override
    public void onDisableCore() {
        // Save data
        this.portalStore.save();
        this.configStore.save();

        Plugin.instance = null;
    }
}
