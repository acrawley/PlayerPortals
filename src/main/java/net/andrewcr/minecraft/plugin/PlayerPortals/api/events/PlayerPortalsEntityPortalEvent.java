package net.andrewcr.minecraft.plugin.PlayerPortals.api.events;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.types.IPortal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalEvent;

public class PlayerPortalsEntityPortalEvent extends EntityPortalEvent {
    @Getter private final IPortal originPortal;
    @Getter private final IPortal destinationPortal;

    public PlayerPortalsEntityPortalEvent(IPortal originPortal, IPortal destinationPortal, Entity entity, Location from, Location to) {
        super(entity, from, to, null);

        this.originPortal = originPortal;
        this.destinationPortal = destinationPortal;
    }
}
