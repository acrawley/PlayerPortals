package net.andrewcr.minecraft.plugin.PlayerPortals.listeners;

import lombok.Data;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.LocationUtil;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.integration.distributedspawns.DistributedSpawnsIntegration;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@Data
public class PortalDestination {
    private final Location location;
    private final String description;
    private final Portal portal;

    public static PortalDestination getPortalDestination(Portal sourcePortal, Player player) {
        return PortalDestination.getPortalDestination(
            sourcePortal.getDestination(),
            !StringUtil.isNullOrEmpty(sourcePortal.getMessage())?sourcePortal.getMessage():sourcePortal.getDestination(),
            player);
    }

    public static PortalDestination getPortalDestinationByName(String destinationName, Player player) {
        return PortalDestination.getPortalDestination(destinationName, destinationName, player);
    }

    private static PortalDestination getPortalDestination(String destinationName, String message, Player player) {
        Portal targetPortal = PortalStore.getInstance().getPortalByName(destinationName);
        if (targetPortal != null) {
            // Target is a portal - destination is one block below its sign (so we consider the block the sign is on when finding a safe location)
            Location targetLocation = targetPortal.getLocation().add(0.5, -1, 0.5);
            targetLocation.setYaw(targetPortal.getSignAngle());

            Location destLocation = LocationUtil.findSafeLocationInColumn(targetLocation);
            if (destLocation == null) {
                // Couldn't find a safe block - use the original location and hope for the best
                destLocation = targetPortal.getLocation().add(0.5, 0, 0.5);
            }

            // Find unobstructed location at destination
            return new PortalDestination(
                destLocation,
                message,
                targetPortal);
        } else {
            World world = Bukkit.getWorld(destinationName);
            if (world != null) {
                // Target is a world - get its spawn point
                Location spawnLoc;
                if (player != null) {
                    spawnLoc = DistributedSpawnsIntegration.getInstance().getPlayerSpawnLocation(world, player);
                } else {
                    spawnLoc = world.getSpawnLocation();
                }

                return new PortalDestination(
                    spawnLoc,
                    world.getName(),
                    null);
            }
        }

        return null;
    }
}
