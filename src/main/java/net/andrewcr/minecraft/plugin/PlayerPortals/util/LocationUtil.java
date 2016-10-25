package net.andrewcr.minecraft.plugin.PlayerPortals.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class LocationUtil {
    public static float getSignAngle(Sign sign) {
        BlockFace face = ((org.bukkit.material.Sign) sign.getData()).getFacing();
        return LocationUtil.blockFaceToYaw(face);
    }

    public static float blockFaceToYaw(BlockFace face) {
        switch (face) {
            case NORTH:
                return -180f;
            case NORTH_NORTH_EAST:
                return -157.5f;
            case NORTH_EAST:
                return -135f;
            case EAST_NORTH_EAST:
                return -112.5f;
            case EAST:
                return -90f;
            case EAST_SOUTH_EAST:
                return -67.5f;
            case SOUTH_EAST:
                return -45f;
            case SOUTH_SOUTH_EAST:
                return -22.5f;
            case SOUTH:
                return 0f;
            case SOUTH_SOUTH_WEST:
                return 22.5f;
            case SOUTH_WEST:
                return 45f;
            case WEST_SOUTH_WEST:
                return 67.5f;
            case WEST:
                return 90f;
            case WEST_NORTH_WEST:
                return 112.5f;
            case NORTH_WEST:
                return 135f;
            case NORTH_NORTH_WEST:
                return 157.5f;
        }

        return 0f;
    }

    public static Location findSafeLocationInColumn(Location startLocation) {
        Location location = LocationUtil.findSafeLocationInColumn(
            startLocation.getWorld(),
            startLocation.getBlockX(),
            startLocation.getBlockY(),
            startLocation.getBlockZ());

        location.setYaw(startLocation.getYaw());
        location.setPitch(startLocation.getPitch());

        return location;
    }

    public static Location findSafeLocationInColumn(World world, int x, int minY, int z) {
        boolean foundSolid = false;
        int airBlocks = 0;

        // Check this column of the world for the first solid block above a minimum height with at least two blocks of air above it
        // NOTE: This seems to occasionally run into trouble with trees
        for (int y = minY; y < 256; y++) {
            Material material = world.getBlockAt(x, y, z).getType();

            if (!foundSolid) {
                // Keep looking for a solid block
                foundSolid = !MaterialUtil.canOccupy(material);
            } else {
                // Found a solid block, now look for 2 blocks of air
                if (MaterialUtil.canOccupy(material)) {
                    airBlocks++;
                } else {
                    // Found non-air, start over
                    airBlocks = 0;
                }

                if (airBlocks == 2) {
                    // Found our two blocks, we're done
                    return new Location(world, x + 0.5, y - 1, z + 0.5);
                }
            }
        }

        return null;
    }


}
