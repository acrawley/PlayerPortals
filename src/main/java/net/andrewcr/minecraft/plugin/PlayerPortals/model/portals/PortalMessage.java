package net.andrewcr.minecraft.plugin.PlayerPortals.model.portals;

import lombok.Getter;

public class PortalMessage {
    //region Private Fields

    @Getter private boolean isError;
    @Getter private String message;

    //endregion

    //region Constructors / Instantiation

    private PortalMessage(String message, boolean isError) {
        this.isError = isError;
        this.message = message;
    }

    public static PortalMessage CreateError(String message) {
        return new PortalMessage(message, true);
    }

    public static PortalMessage CreateWarning(String message) {
        return new PortalMessage(message, false);
    }

    //endregion
}
