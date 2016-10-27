package net.andrewcr.minecraft.plugin.PlayerPortals.events;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.Vector;

public class PlayerPortalsEntityPortalExitEvent extends EntityPortalExitEvent {
    @Getter
    private final Portal originPortal;
    @Getter
    private final Portal destinationPortal;

    public PlayerPortalsEntityPortalExitEvent(Portal originPortal, Portal destinationPortal, Entity entity, Location from, Location to, Vector before, Vector after) {
        super(entity, from, to, before, after);

        this.originPortal = originPortal;
        this.destinationPortal = destinationPortal;
    }
}
