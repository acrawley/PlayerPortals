package net.andrewcr.minecraft.plugin.PlayerPortals.model.portals;

import lombok.Data;

@Data
public class PortalExitHeading {
    private final float yaw;
    private final float pitch;
    private final float velocityMultiplier;
    private final boolean isAbsoluteVelocity;
}
