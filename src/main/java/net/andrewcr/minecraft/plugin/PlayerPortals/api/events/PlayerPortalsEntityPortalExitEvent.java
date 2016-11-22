package net.andrewcr.minecraft.plugin.PlayerPortals.api.events;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.types.IPortal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.Vector;

public class PlayerPortalsEntityPortalExitEvent extends EntityPortalExitEvent {
    @Getter private final IPortal originPortal;
    @Getter private final IPortal destinationPortal;

    public PlayerPortalsEntityPortalExitEvent(IPortal originPortal, IPortal destinationPortal, Entity entity, Location from, Location to, Vector before, Vector after) {
        super(entity, from, to, before, after);

        this.originPortal = originPortal;
        this.destinationPortal = destinationPortal;
    }
}
