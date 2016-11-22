package net.andrewcr.minecraft.plugin.PlayerPortals.api;

import net.andrewcr.minecraft.plugin.PlayerPortals.api.types.IPortal;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IPlayerPortalsApi {
    /**
     * Gets the portal with the specified name
     *
     * @param name The name of the portal
     * @return The portal, or null if no portal with the given name exists
     */
    IPortal getPortalByName(String name);

    /**
     * Sends the player to the specified destination
     *
     * @param player      The player to teleport
     * @param destination The destination to which the player should be teleported, as would be specified on the third line of a portal sign.
     * @param sendMessage True if the standard message should be sent to the player, false otherwise
     * @return true if the teleport was sucessful, false otherwise
     */
    boolean teleportPlayer(Player player, String destination, boolean sendMessage);

    /**
     * Gets the location a player would be sent to when telelporting to the specified destination
     *
     * @param player      The player who would be teleported
     * @param destination The destination to which the player would be teleported
     * @return The location to which the player would be teleported, or null if the destination is invalid
     */
    Location getTeleportLocation(Player player, String destination);
}
