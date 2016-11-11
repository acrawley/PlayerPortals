package net.andrewcr.minecraft.plugin.PlayerPortals.model.portals;

import lombok.Getter;
import lombok.Synchronized;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.LocationUtil;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class Portal {
    private static final String SIGN_POSITION_KEY = "SignPosition";
    private static final String SIGN_ANGLE_KEY = "SignAngle";
    private static final String OWNER_KEY = "Owner";
    private static final String DESTINATION_KEY = "Destination";
    private static final String MESSAGE_KEY = "Message";
    private static final String PORTAL_EXIT_HEADING_KEY = "PortalExitHeading";

    private final Object configLock = PortalStore.getInstance().getSyncObj();

    private final Location location;
    @Getter private final float signAngle;
    @Getter private final String name;
    @Getter private final String destination;
    @Getter private UUID owner;
    @Getter private String message;
    @Getter private PortalExitHeading exitHeading;

    public Portal(Location location, float signAngle, UUID owner, String name, String destination, String message) {
        this.location = location;
        this.signAngle = signAngle;
        this.owner = owner;
        this.name = name;
        this.destination = destination;
        this.message = message;
    }

    //region Serialization

    public static Portal loadFrom(ConfigurationSection portalSection) {
        String name =  portalSection.getName();
        Location signPosition;

        float signAngle = 0;
        UUID owner = null;
        String description = null;
        String destination = null;

        // All portals must have a position
        if (!portalSection.contains(SIGN_POSITION_KEY)) {
            return null;
        }

        signPosition = LocationUtil.locationFromString(portalSection.getString(SIGN_POSITION_KEY));

        if (portalSection.contains(SIGN_ANGLE_KEY)) {
            signAngle = (float) portalSection.getDouble(SIGN_ANGLE_KEY);
        }

        if (portalSection.contains(OWNER_KEY)) {
            owner = UUID.fromString(portalSection.getString(OWNER_KEY));
        }

        if (portalSection.contains(MESSAGE_KEY)) {
            description = portalSection.getString(MESSAGE_KEY);
        }

        if (portalSection.contains(DESTINATION_KEY)) {
            destination = portalSection.getString(DESTINATION_KEY);
        }

        Portal portal = new Portal(signPosition, signAngle, owner, name, destination, description);

        if (portalSection.contains(PORTAL_EXIT_HEADING_KEY)) {
            portal.setExitHeading(PortalExitHeading.fromString(portalSection.getString(PORTAL_EXIT_HEADING_KEY)));
        }

        return portal;
    }

    @Synchronized("configLock")
    public void save(ConfigurationSection portals) {
        ConfigurationSection portal = portals.createSection(this.name);

        portal.set(SIGN_POSITION_KEY, LocationUtil.locationToIntString(location, true, false, false));

        if (this.signAngle != 0) {
            portal.set(SIGN_ANGLE_KEY, this.signAngle);
        }

        if (!StringUtil.isNullOrEmpty(this.destination)) {
            portal.set(DESTINATION_KEY, this.destination);
        }

        if (!StringUtil.isNullOrEmpty(this.message)) {
            portal.set(MESSAGE_KEY, this.message);
        }

        if (this.owner != null) {
            portal.set(OWNER_KEY, this.owner.toString());
        }

        if (this.exitHeading != null) {
            portal.set(PORTAL_EXIT_HEADING_KEY, this.exitHeading.toString());
        }
    }

    //endregion

    //region Validation

    public PortalMessage validatePortal() {
        // Errors - prevent the portal from being created
        if (StringUtil.isNullOrEmpty(this.name)) {
            return PortalMessage.CreateError("Portal does not have a name!");
        }

        Portal dupePortal = PortalStore.getInstance().getPortalByName(this.name);
        if (dupePortal != null && dupePortal != this) {
            return PortalMessage.CreateError("A portal named '" + this.name + "' already exists!");
        }

        if (Bukkit.getWorld(this.name) != null) {
            return PortalMessage.CreateError("Portal cannot have the same name as world '" + this.name + "'!");
        }

        if (StringUtil.equals(this.name, this.destination)) {
            return PortalMessage.CreateError("Portal cannot target itself!");
        }

        if (this.location.getWorld() == null) {
            return PortalMessage.CreateError("Unknown world!");
        }

        if (!(this.location.getBlock().getState() instanceof Sign)) {
            return PortalMessage.CreateError("No portal sign found at location (" +
                this.location.getBlockX() + ", " +
                this.location.getBlockY() + ", " +
                this.location.getBlockZ() + ")!");
        }

        // Warnings
        if (!StringUtil.isNullOrEmpty(this.destination) &&
            PortalStore.getInstance().getPortalByName(this.destination) == null &&
            Bukkit.getWorld(this.destination) == null) {
            return PortalMessage.CreateWarning("Unknown destination '" + this.destination + "'!");
        }

        return null;
    }

    //endregion

    public Location getLocation() {
        // Return a clone to protect this from modification
        return this.location.clone();
    }

    @Synchronized("configLock")
    public void setExitHeading(PortalExitHeading exitHeading) {
        this.exitHeading = exitHeading;
        PortalStore.getInstance().notifyChanged();
    }

    @Synchronized("configLock")
    public void setOwner(UUID owner) {
        this.owner = owner;
        PortalStore.getInstance().notifyChanged();
    }

    @Synchronized("configLock")
    public void setMessage(String message) {
        this.message = message;
        PortalStore.getInstance().notifyChanged();
    }
}
