package net.andrewcr.minecraft.plugin.PlayerPortals.integration.distributedspawns;

import net.andrewcr.minecraft.plugin.DistributedSpawns.api.IDistributedSpawnsApi;
import net.andrewcr.minecraft.plugin.PlayerPortals.Plugin;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.config.ConfigStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class DistributedSpawnsIntegration {
    private IDistributedSpawnsApi dsApi;

    public static DistributedSpawnsIntegration getInstance() {
        return Plugin.getInstance().getDistributedSpawnsIntegration();
    }

    public DistributedSpawnsIntegration() {
        if (!ConfigStore.getInstance().isDistributedSpawnsIntegrationEnabled()) {
            Plugin.getInstance().getLogger().info("DistributedSpawns integration disabled in config.yml!");
            return;
        }

        org.bukkit.plugin.Plugin dsPlugin = Bukkit.getPluginManager().getPlugin("DistributedSpawns");
        if (dsPlugin == null) {
            Plugin.getInstance().getLogger().info("DistributedSpawns integration disabled - failed to get plugin instance!");
            return;
        }

        if (!dsPlugin.isEnabled()) {
            Plugin.getInstance().getLogger().info("DistributedSpawns integration disabled - DistributedSpawns plugin is not enabled!");
            return;
        }

        if (!(dsPlugin instanceof IDistributedSpawnsApi)) {
            Plugin.getInstance().getLogger().info("DistributedSpawns integration disabled - plugin instance not of expected type!");
            return;
        }

        this.dsApi = (IDistributedSpawnsApi) dsPlugin;

        Plugin.getInstance().getLogger().info("DistributedSpawns integration enabled!");
    }

    public Location getPlayerSpawnLocation(World world, Player player) {
        if (dsApi != null) {
            return dsApi.getPlayerSpawnLocation(world, player);
        }

        return world.getSpawnLocation();
    }
}
