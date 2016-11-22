package net.andrewcr.minecraft.plugin.PlayerPortals.api.types;

import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.PortalExitHeading;
import org.bukkit.Location;

import java.util.UUID;

public interface IPortal {
    /**
     * Gets the location of the sign that represents this portal.  Note that this is not the exact destination of the portal.
     *
     * @return The sign location
     */
    Location getLocation();

    /**
     * Gets the "yaw" angle of the sign that represents this portal.
     *
     * @return The sign angle
     */
    float getSignAngle();

    /**
     * Gets the name of the portal (The second line of the portal's sign)
     *
     * @return The portal name
     */
    String getName();

    /**
     * Gets the name of the portal's destination (The third line of the portal's sign).  May be null.
     *
     * @return The destination name
     */
    String getDestination();

    /**
     * Gets the UUID of the player who created the portal.  May be null, e.g. if the portal was imported from another plugin.
     *
     * @return The creator's UUID
     */
    UUID getOwner();

    /**
     * Gets the message displayed when a player enters this portal (The fourth line of the portal's sign).  May be null.
     *
     * @return The entry message
     */
    String getMessage();

    /**
     * Gets the exit heading of the portal.  May be null if this isn't customized.
     *
     * @return The exit heading
     */
    PortalExitHeading getExitHeading();

    /**
     * Sets a custom property on the portal.  This is stored in the portal metadata.  Setting a property to null removes it.
     *
     * @param property The property to set
     * @param value    The value to which the property should be set
     */
    void setProperty(String property, String value);

    /**
     * Gets a custom property set with the "setProperty" method.
     *
     * @param property The name of the property
     * @return The property's value, or null if it is unset
     */
    String getProperty(String property);
}
