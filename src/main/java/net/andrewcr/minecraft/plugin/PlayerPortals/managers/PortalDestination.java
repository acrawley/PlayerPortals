package net.andrewcr.minecraft.plugin.PlayerPortals.managers;

import lombok.Data;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.LocationUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public @Data
class PortalDestination {
    private final Location location;
    private final String description;
    private final Portal portal;

    public static PortalDestination getPortalDestination(String destinationName) {
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
                !StringUtil.isNullOrEmpty(targetPortal.getDescription()) ? targetPortal.getDescription() : targetPortal.getName(),
                targetPortal);
        } else {
            World world = Bukkit.getWorld(destinationName);
            if (world != null) {
                // Target is a world - get its spawn point
                return new PortalDestination(
                    world.getSpawnLocation(),
                    world.getName(),
                    null);
            }
        }

        return null;
    }
}
