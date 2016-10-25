package net.andrewcr.minecraft.plugin.PlayerPortals.util;

import org.bukkit.Material;

public class MaterialUtil {
    public static boolean canOccupy(Material material) {
        switch (material) {
            case SIGN_POST:
            case WALL_SIGN:
            case WOOD_PLATE:
            case TRAP_DOOR:
            case GOLD_PLATE:
            case IRON_PLATE:
            case DAYLIGHT_DETECTOR:
            case IRON_TRAPDOOR:
            case STANDING_BANNER:
            case WALL_BANNER:
            case DAYLIGHT_DETECTOR_INVERTED:
                // Material.isSolid indicates that these blocks are solid, but we treat them as if they aren't
                return true;

            case END_ROD:
            case CHORUS_PLANT:
            case CHORUS_FLOWER:
            case END_GATEWAY:
                // Material.isSolid indicates that these blocks aren't solid, but we treat them as if they are
                return false;

            default:
                // This is documented as returning whether or not a player can pass through a block, but it's really
                //  more like whether or not water will flow through the block.  Aside from the exceptions above, it's
                //  pretty much the same.
                return !material.isSolid();
        }
    }
}
