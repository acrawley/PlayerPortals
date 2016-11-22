package net.andrewcr.minecraft.plugin.PlayerPortals.internal.integration.dynmap;

import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.Plugin;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.config.ConfigStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.PortalStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public class DynmapIntegration {
    //region Private Fields

    private static final String PORTAL_MARKER_SET_ID = "PlayerPortals.Markers.1.0";

    private MarkerAPI markerApi = null;
    private MarkerSet spawnMarkerSet;

    //endregion

    //region Singleton Instance

    public static DynmapIntegration getInstance() {
        return Plugin.getInstance().getDynmapIntegration();
    }

    //endregion

    //region Constructor

    public DynmapIntegration() {
        if (!ConfigStore.getInstance().isDynmapIntegrationEnabled()) {
            Plugin.getInstance().getLogger().info("Dynmap integration disabled in config.yml!");
            return;
        }

        org.bukkit.plugin.Plugin dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");

        if (dynmapPlugin == null) {
            Plugin.getInstance().getLogger().info("Dynmap integration disabled - failed to get plugin instance!");
            return;
        }

        if (!dynmapPlugin.isEnabled()) {
            Plugin.getInstance().getLogger().info("Dynmap integration disabled - Dynmap plugin is not enabled!");
            return;
        }

        if (!(dynmapPlugin instanceof DynmapAPI)) {
            Plugin.getInstance().getLogger().info("Dynmap integration disabled - plugin instance not of expected type!");
            return;
        }

        DynmapAPI dynmapApi = (DynmapAPI) dynmapPlugin;

        if (!dynmapApi.markerAPIInitialized()) {
            Plugin.getInstance().getLogger().info("Dynmap integration disabled - Marker API is not ready!");
            return;
        }

        this.markerApi = dynmapApi.getMarkerAPI();
        if (this.markerApi == null) {
            Plugin.getInstance().getLogger().info("Dynmap integration disabled - failed to get Marker API!");
            return;
        }

        Plugin.getInstance().getLogger().info("Dynmap integration enabled - Dynmap version: " + dynmapApi.getDynmapVersion()
            + ", Dynmap-Core version: " + dynmapApi.getDynmapCoreVersion());

        for (World world : Bukkit.getWorlds()) {
            this.init(world);
        }
    }

    // endregion

    //region Public API

    public void notifyPortalCreated(Portal portal) {
        if (this.markerApi == null || portal.getLocation() == null) {
            return;
        }

        MarkerSet markerSet = this.getPortalMarkerSet();
        if (markerSet == null) {
            return;
        }

        String markerId = this.getPortalMarkerId(portal);
        Location portalLoc = portal.getLocation();

        // Create marker
        markerSet.createMarker(
            markerId,
            !StringUtil.isNullOrEmpty(portal.getMessage()) ? portal.getMessage() : portal.getName(),
            portalLoc.getWorld().getName(),
            portalLoc.getX(),
            portalLoc.getY(),
            portalLoc.getZ(),
            markerApi.getMarkerIcon("portal"),
            false);
    }

    public void notifyPortalRemoved(Portal portal) {
        if (this.markerApi == null || portal.getLocation() == null) {
            return;
        }

        MarkerSet markerSet = this.getPortalMarkerSet();
        if (markerSet == null) {
            return;
        }

        String markerId = this.getPortalMarkerId(portal);
        Marker portalMarker = markerSet.findMarker(markerId);

        if (portalMarker != null) {
            portalMarker.deleteMarker();
        }
    }

    private void init(World world) {
        if (this.markerApi == null) {
            return;
        }

        // Enable - create markers for all portals in the specified world
        Iterable<Portal> portals = PortalStore.getInstance().getPortalsInWorld(world);

        for (Portal portal : portals) {
            this.notifyPortalCreated(portal);
        }
    }

    //endregion

    //region Helpers

    private MarkerSet getPortalMarkerSet() {
        if (this.spawnMarkerSet == null) {
            this.spawnMarkerSet = markerApi.getMarkerSet(PORTAL_MARKER_SET_ID);

            if (this.spawnMarkerSet == null) {
                this.spawnMarkerSet = markerApi.createMarkerSet(PORTAL_MARKER_SET_ID, "Portals", null, false);
                this.spawnMarkerSet.setHideByDefault(true);
            }
        }

        return this.spawnMarkerSet;
    }

    private String getPortalMarkerId(Portal portal) {
        return "portal-" + portal.getName();
    }

    //endregion
}

