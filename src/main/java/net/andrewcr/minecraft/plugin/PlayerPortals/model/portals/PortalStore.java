package net.andrewcr.minecraft.plugin.PlayerPortals.model.portals;

import net.andrewcr.minecraft.plugin.PlayerPortals.Plugin;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.ConfigurationFileBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PortalStore extends ConfigurationFileBase {
    //region Private Fields

    private List<Portal> portals;

    //endregion

    //region Constructor

    public PortalStore() {
        this.portals = new ArrayList<>();

        ConfigurationSerialization.registerClass(Portal.class);
    }

    //endregion

    //region Singleton

    public static PortalStore getInstance() {
        return Plugin.getInstance().getPortalStore();
    }

    //endregion

    //region Serialization

    public void loadCore() {
        File portalFile = new File(Plugin.getInstance().getDataFolder(), "portals.yml");
        if (portalFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(portalFile);
            String version = config.getString("ConfigurationVersion");

            switch (version) {
                case "1.0":
                    this.loadV1_0Config(config);
                    return;

                default:
                    Plugin.getInstance().getLogger().severe("Unknown portal configuration version '" + version + "'!");
            }
        }
    }

    private void loadV1_0Config(YamlConfiguration config) {
        List<Portal> configPortals = (List<Portal>) config.getList("Portals");
        if (configPortals != null) {
            this.portals = configPortals;
        }
    }

    public void saveCore() {
        File portalFile = new File(Plugin.getInstance().getDataFolder(), "portals.yml");

        YamlConfiguration config = new YamlConfiguration();
        config.set("ConfigurationVersion", "1.0");

        config.set("Portals", this.portals);

        try {
            config.save(portalFile);
        } catch (IOException e) {
            Plugin.getInstance().getLogger().severe("Failed to save portal configuration: " + e.toString());
        }
    }

    //endregion

    //region Public API

    public Portal getPortalByName(String name) {
        for (Portal portal : this.portals) {
            if (portal.getName() != null && portal.getName().equals(name)) {
                return portal;
            }
        }

        return null;
    }

    public Portal getPortalByLocation(Location location) {
        for (Portal portal : this.portals) {
            if (portal.getLocation().equals(location)) {
                return portal;
            }
        }

        return null;
    }

    public Portal getClosestPortal(Location location) {
        double closestDistance = Double.MAX_VALUE;
        Portal closestPortal = null;

        // Find the closest portal in the same world
        for (Portal portal : this.getPortalsInWorld(location.getWorld())) {
            double distance = portal.getLocation().distance(location);
            if (distance < 5 && distance < closestDistance) {
                closestDistance = distance;
                closestPortal = portal;
            }
        }

        return closestPortal;
    }

    public Iterable<Portal> getPortalsByDestination(String destination) {
        return this.portals.stream()
            .filter(portal -> StringUtil.equals(portal.getDestination(), destination))
            .collect(Collectors.toList());
    }

    public Iterable<Portal> getPortalsInWorld(World world) {
        return this.portals.stream()
            .filter(portal -> portal.getLocation().getWorld() == world)
            .collect(Collectors.toList());
    }

    public Iterable<Portal> getPortals() {
        return this.portals;
    }

    public void addPortal(Portal portal) {
        this.portals.add(portal);
        this.notifyChanged();
    }

    public void removePortal(Portal portal) {
        this.portals.remove(portal);
        this.notifyChanged();
    }

    //endregion
}
