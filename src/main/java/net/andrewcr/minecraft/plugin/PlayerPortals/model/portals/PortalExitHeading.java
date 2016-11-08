package net.andrewcr.minecraft.plugin.PlayerPortals.model.portals;

import lombok.Data;

@Data
public class PortalExitHeading {
    private final float yaw;
    private final float pitch;
    private final float velocityMultiplier;
    private final boolean isAbsoluteVelocity;

    public static PortalExitHeading fromString(String text) {
        String[] headingParts = text.split(",");

        if (headingParts.length != 3) {
            // Invalid format
            return null;
        }

        String velocityRaw = headingParts[2].trim();

        boolean isAbsoluteVelocity = true;
        if (velocityRaw.startsWith("x")) {
            isAbsoluteVelocity = false;
            velocityRaw = velocityRaw.substring(1);
        }

        try {
            return new PortalExitHeading(
                Double.valueOf(headingParts[0].trim()).floatValue(),
                Double.valueOf(headingParts[1].trim()).floatValue(),
                Double.valueOf(velocityRaw).floatValue(),
                isAbsoluteVelocity);
        } catch (NumberFormatException ex) {
            // Invalid format
            return null;
        }
    }

    @Override
    public String toString() {
        return this.yaw + ", "
            + this.pitch + ", "
            + (this.isAbsoluteVelocity ? "" : "x")
            + this.velocityMultiplier;
    }
}
