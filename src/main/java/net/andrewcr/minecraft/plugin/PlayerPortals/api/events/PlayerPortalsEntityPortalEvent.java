package net.andrewcr.minecraft.plugin.PlayerPortals.api.events;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalEvent;

public class PlayerPortalsEntityPortalEvent extends EntityPortalEvent {
    @Getter private final Portal originPortal;
    @Getter private final Portal destinationPortal;

    public PlayerPortalsEntityPortalEvent(Portal originPortal, Portal destinationPortal, Entity entity, Location from, Location to) {
        super(entity, from, to, null);

        this.originPortal = originPortal;
        this.destinationPortal = destinationPortal;
    }
}
