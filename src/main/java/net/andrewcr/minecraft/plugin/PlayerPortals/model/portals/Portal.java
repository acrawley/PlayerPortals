package net.andrewcr.minecraft.plugin.PlayerPortals.model.portals;

import lombok.Getter;
import lombok.Synchronized;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("Portal")
public class Portal implements ConfigurationSerializable {
    private final Object configLock = PortalStore.getInstance().getSyncObj();

    private final Location location;
    @Getter
    private final float signAngle;
    @Getter
    private final String name;
    @Getter
    private final String destination;
    @Getter
    private UUID owner;
    @Getter
    private String description;
    private PortalExitHeading exitHeading;

    public Portal(Location location, float signAngle, UUID owner, String name, String destination, String description) {
        this.location = location;
        this.signAngle = signAngle;
        this.owner = owner;
        this.name = name;
        this.destination = destination;
        this.description = description;
    }

    //region Serialization

    public static Portal deserialize(Map<String, Object> data) {
        UUID owner = null;
        Location signPosition;
        float signAngle = 0;
        String name = null;
        String destination = null;
        String message = null;

        // All portals must have a world and position
        if (!data.containsKey("World") || !data.containsKey("SignPosition")) {
            return null;
        }

        World world = Bukkit.getWorld((String) data.get("World"));
        String[] positionParts = ((String) data.get("SignPosition")).split(",");
        signPosition = new Location(
            world,
            Integer.parseInt(positionParts[0].trim()),
            Integer.parseInt(positionParts[1].trim()),
            Integer.parseInt(positionParts[2].trim()));

        if (data.containsKey("SignAngle")) {
            signAngle = ((Double) data.get("SignAngle")).floatValue();
        }

        if (data.containsKey("Owner")) {
            owner = UUID.fromString((String) data.get("Owner"));
        }

        if (data.containsKey("Name")) {
            name = (String) data.get("Name");
        }

        if (data.containsKey("Description")) {
            message = (String) data.get("Description");
        }

        if (data.containsKey("Destination")) {
            destination = (String) data.get("Destination");
        }

        Portal portal = new Portal(signPosition, signAngle, owner, name, destination, message);

        if (data.containsKey("PortalExitHeading")) {
            String[] headingParts = ((String) data.get("PortalExitHeading")).split(",");

            String[] velocityParts = headingParts[2].trim().split(" ");

            portal.setExitHeading(new PortalExitHeading(
                Double.valueOf(headingParts[0].trim()).floatValue(),
                Double.valueOf(headingParts[1].trim()).floatValue(),
                Double.valueOf(velocityParts[0].trim()).floatValue(),
                velocityParts.length == 2 && StringUtil.equalsIgnoreCase(velocityParts[1], "abs")));
        }

        return portal;
    }

    @Synchronized("configLock")
    public Map<String, Object> serialize() {
        // Use a LinkedHashMap to maintain property ordering in serialized format
        Map<String, Object> data = new LinkedHashMap<>();

        if (!StringUtil.isNullOrEmpty(this.name)) {
            data.put("Name", this.name);
        }

        data.put("World", this.location.getWorld().getName());
        data.put("SignPosition", this.location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());

        if (this.signAngle != 0) {
            data.put("SignAngle", this.signAngle);
        }

        if (!StringUtil.isNullOrEmpty(this.destination)) {
            data.put("Destination", this.destination);
        }

        if (!StringUtil.isNullOrEmpty(this.description)) {
            data.put("Description", this.description);
        }

        if (this.owner != null) {
            data.put("Owner", this.owner.toString());
        }

        if (this.exitHeading != null) {
            data.put("PortalExitHeading", this.exitHeading.getYaw() + ", "
                + this.exitHeading.getPitch() + ", "
                + this.exitHeading.getVelocityMultiplier()
                + (this.exitHeading.isAbsoluteVelocity() ? " abs" : ""));
        }

        return data;
    }

    //endregion

    //region Validation

    public PortalMessage validatePortal() {
        // Errors - prevent the portal from being created
        if (!StringUtil.isNullOrEmpty(this.name)) {
            Portal dupePortal = PortalStore.getInstance().getPortalByName(this.name);
            if (dupePortal != null && dupePortal != this) {
                return PortalMessage.CreateError("A portal named '" + this.name + "' already exists!");
            }

            if (Bukkit.getWorld(this.name) != null) {
                return PortalMessage.CreateError("Portal cannot have the same name as world '" + this.name + "'!");
            }
        }

        if (StringUtil.isNullOrEmpty(this.name) && StringUtil.isNullOrEmpty(this.destination)) {
            return PortalMessage.CreateError("Portal must have a name, a destination, or both!");
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

    public PortalExitHeading getExitHeading() {
        return this.exitHeading;
    }

    @Synchronized("configLock")
    public void setOwner(UUID owner) {
        this.owner = owner;
        PortalStore.getInstance().notifyChanged();
    }

    @Synchronized("configLock")
    public void setDescription(String description) {
        this.description = description;
        PortalStore.getInstance().notifyChanged();
    }

    @Synchronized("configLock")
    public void setExitHeading(PortalExitHeading exitHeading) {
        this.exitHeading = exitHeading;
        PortalStore.getInstance().notifyChanged();
    }
}
