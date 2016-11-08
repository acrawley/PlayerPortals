package net.andrewcr.minecraft.plugin.PlayerPortals.model.portals;

import net.andrewcr.minecraft.plugin.BasePluginLib.config.ConfigurationFileBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.Plugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PortalStore extends ConfigurationFileBase {
    //region Private Fields

    private static final String CONFIGURATION_VERSION_KEY = "ConfigurationVersion";
    private static final String PORTALS_KEY = "Portals";

    private Map<String, Portal> portals;


    //endregion

    //region Constructor

    public PortalStore() {
        super(Plugin.getInstance());
        this.portals = new LinkedHashMap<>();
    }

    //endregion

    //region Singleton

    public static PortalStore getInstance() {
        return Plugin.getInstance().getPortalStore();
    }

    //endregion

    //region Serialization


    @Override
    protected String getFileName() {
        return "portals.yml";
    }

    public void loadCore(YamlConfiguration configuration) {
        String version = configuration.getString(CONFIGURATION_VERSION_KEY);
        switch (version) {
            case "1.0":
                this.loadV1_0Config(configuration);
                return;

            default:
                Plugin.getInstance().getLogger().severe("Unknown portal configuration version '" + version + "'!");
        }
    }

    private void loadV1_0Config(YamlConfiguration config) {
        ConfigurationSection portals = config.getConfigurationSection(PORTALS_KEY);
        if (portals != null) {
            for (String portalName : portals.getKeys(false)) {
                this.addPortal(Portal.loadFrom(portals.getConfigurationSection(portalName)));
            }
        }

        Plugin.getInstance().getLogger().info("Loaded " + this.portals.size() + " portal(s)!");
    }

    public void saveCore(YamlConfiguration configuration) {
        configuration.set(CONFIGURATION_VERSION_KEY, "1.0");

        ConfigurationSection portals = configuration.createSection(PORTALS_KEY);
        for (Portal portal : this.portals.values()) {
            portal.save(portals);
        }

        Plugin.getInstance().getLogger().info("Saved " + this.portals.size() + " portal(s)!");
    }

    //endregion

    //region Public API

    public Portal getPortalByName(String name) {
        if (this.portals.containsKey(name)) {
            return this.portals.get(name);
        }

        return null;
    }

    public Portal getPortalByLocation(Location location) {
        return this.portals.values().stream()
            .filter(p -> p.getLocation().equals(location))
            .findAny()
            .orElse(null);
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
        return this.portals.values().stream()
            .filter(portal -> StringUtil.equals(portal.getDestination(), destination))
            .collect(Collectors.toList());
    }

    public Iterable<Portal> getPortalsInWorld(World world) {
        return this.portals.values().stream()
            .filter(portal -> portal.getLocation().getWorld() == world)
            .collect(Collectors.toList());
    }

    public Iterable<Portal> getPortals() {
        return this.portals.values();
    }

    public void addPortal(Portal portal) {
        this.portals.put(portal.getName(), portal);
        this.notifyChanged();
    }

    public void removePortal(Portal portal) {
        this.portals.remove(portal.getName());
        this.notifyChanged();
    }

    //endregion
}
