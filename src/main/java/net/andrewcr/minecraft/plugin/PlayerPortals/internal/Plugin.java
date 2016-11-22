package net.andrewcr.minecraft.plugin.PlayerPortals.internal;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.BasePluginLib.plugin.PluginBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.Version;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.IPlayerPortalsApi;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.types.IPortal;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.commands.*;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.integration.distributedspawns.DistributedSpawnsIntegration;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.integration.dynmap.DynmapIntegration;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.listeners.PortalDestination;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.listeners.PortalListener;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.listeners.SignListener;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.config.ConfigStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.PortalStore;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Plugin extends PluginBase implements IPlayerPortalsApi{
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

    //region IPlayerPortalsApi Implementation

    @Override
    public IPortal getPortalByName(String name) {
        return this.portalStore.getPortalByName(name);
    }

    @Override
    public boolean teleportPlayer(Player player, String destinationName, boolean sendMessage) {
        PortalDestination destination = PortalDestination.getPortalDestinationByName(destinationName, player);
        if (destination == null) {
            return false;
        }

        player.teleport(destination.getLocation());

        if (sendMessage) {
            player.sendMessage("You warped to '" + destination.getDescription() + "'!");
        }

        return true;
    }

    @Override
    public Location getTeleportLocation(Player player, String destinationName) {
        PortalDestination destination = PortalDestination.getPortalDestinationByName(destinationName, player);
        if (destination == null) {
            return null;
        }

        return destination.getLocation();
    }

    //endregion
}
