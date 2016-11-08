package net.andrewcr.minecraft.plugin.PlayerPortals.events;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerPortalsPlayerPortalEvent extends PlayerPortalEvent {
    @Getter private final Portal originPortal;
    @Getter private final Portal destinationPortal;

    public PlayerPortalsPlayerPortalEvent(Portal originPortal, Portal destinationPortal, Player player, Location from, Location to) {
        super(player, from, to, null, TeleportCause.PLUGIN);

        this.originPortal = originPortal;
        this.destinationPortal = destinationPortal;
    }
}
