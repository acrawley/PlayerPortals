package net.andrewcr.minecraft.plugin.PlayerPortals.api.events;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.types.IPortal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerPortalsPlayerPortalEvent extends PlayerPortalEvent {
    @Getter private final IPortal originPortal;
    @Getter private final IPortal destinationPortal;

    public PlayerPortalsPlayerPortalEvent(IPortal originPortal, IPortal destinationPortal, Player player, Location from, Location to) {
        super(player, from, to, null, TeleportCause.PLUGIN);

        this.originPortal = originPortal;
        this.destinationPortal = destinationPortal;
    }
}
