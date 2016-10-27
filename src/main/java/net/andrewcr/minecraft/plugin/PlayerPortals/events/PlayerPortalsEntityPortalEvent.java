package net.andrewcr.minecraft.plugin.PlayerPortals.events;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalEvent;

public class PlayerPortalsEntityPortalEvent extends EntityPortalEvent {
    @Getter
    private final Portal originPortal;
    @Getter
    private final Portal destinationPortal;

    public PlayerPortalsEntityPortalEvent(Portal originPortal, Portal destinationPortal, Entity entity, Location from, Location to, TravelAgent pta) {
        super(entity, from, to, pta);

        this.originPortal = originPortal;
        this.destinationPortal = destinationPortal;
    }
}
