package net.andrewcr.minecraft.plugin.PlayerPortals.listeners;

import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.events.PlayerPortalsEntityPortalEvent;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.events.PlayerPortalsEntityPortalExitEvent;
import net.andrewcr.minecraft.plugin.PlayerPortals.api.events.PlayerPortalsPlayerPortalEvent;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.config.ConfigStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.util.Vector;

public class PortalListener implements Listener {
    //region Private Fields

    private final PortalUsageTracker tracker;

    //endregion

    //region Constructor

    public PortalListener() {
        this.tracker = new PortalUsageTracker();
    }

    //endregion

    //region Event Listeners

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Track the player's movement so we know when they've moved far enough from a portal to teleport again
        this.tracker.updateEntityLocation(event.getPlayer());
    }

    @EventHandler
    // This event is fired on every tick where the entity is touching a portal
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!this.tracker.canEnterPortal(event.getEntity())) {
            // The entity teleported too recently or is too close to the last teleport point
            return;
        }

        Portal enteredPortal = PortalStore.getInstance().getClosestPortal(event.getLocation());
        if (enteredPortal == null) {
            // Not one of our portals, don't care
            return;
        }

        // Set the reference point to the entry location
        this.tracker.setReferencePoint(event.getEntity(), event.getLocation());

        if (StringUtil.isNullOrEmpty(enteredPortal.getDestination())) {
            event.getEntity().sendMessage(ChatColor.YELLOW + "Portal does not have a destination!");
            return;
        }

        // Find destination
        PortalDestination destination = PortalDestination.getPortalDestination(
            enteredPortal.getDestination(),
            (event.getEntity() instanceof Player) ? (Player) event.getEntity() : null);

        if (destination == null) {
            event.getEntity().sendMessage(ChatColor.YELLOW + "Unknown destination '" + enteredPortal.getDestination() + "'!");
            return;
        }

        if (destination.getPortal() != null) {
            if (destination.getPortal().getExitHeading() != null) {
                // If the portal has an exit vector set, use that to set our exit direction
                destination.getLocation().setYaw(destination.getPortal().getSignAngle() + destination.getPortal().getExitHeading().getYaw());

                // Minecraft considers negative pitch to be up and positive pitch to be down
                destination.getLocation().setPitch(-destination.getPortal().getExitHeading().getPitch());
            } else if (event.getLocation().getBlock().getState().getType() != Material.ENDER_PORTAL) {
                // Otherwise, Unless we're entering an end portal, maintain relative angle between player and entry portal
                destination.getLocation().setYaw(destination.getPortal().getSignAngle() + ((event.getEntity().getLocation().getYaw() - enteredPortal.getSignAngle()) - 180));
            }
        }

        // Send portal event
        if (this.sendPortalEvent(enteredPortal, destination.getPortal(), event.getEntity(), event.getEntity().getLocation(), destination.getLocation())) {
            // Teleport was cancelled
            return;
        }

        // Move the reference point to the exit location
        this.tracker.setReferencePoint(event.getEntity(), destination.getLocation());

        // Conserve momentum
        Vector velocityBefore = event.getEntity().getVelocity().clone();
        Vector velocityAfter;

        if (ConfigStore.getInstance().isMomentumConserved() && destination.getPortal() != null) {
            velocityAfter = destination.getLocation().getDirection().multiply(velocityBefore.length());
            if (destination.getPortal().getExitHeading() != null) {
                if (destination.getPortal().getExitHeading().isAbsoluteVelocity()) {
                    // Set velocity to the value in the heading
                    velocityAfter = destination.getLocation().getDirection().multiply(destination.getPortal().getExitHeading().getVelocityMultiplier());
                } else {
                    // Apply the portal's velocity multiplier
                    velocityAfter = velocityAfter.multiply(destination.getPortal().getExitHeading().getVelocityMultiplier());
                }
            }
        } else {
            velocityAfter = new Vector();
        }

        PlayerPortalsEntityPortalExitEvent exitEvent = new PlayerPortalsEntityPortalExitEvent(
            enteredPortal,
            destination.getPortal(),
            event.getEntity(),
            event.getEntity().getLocation(),
            destination.getLocation(),
            velocityBefore,
            velocityAfter
        );

        // Send portal exit event
        Bukkit.getServer().getPluginManager().callEvent(exitEvent);

        event.getEntity().teleport(destination.getLocation());
        event.getEntity().sendMessage("You warped to '" + destination.getDescription() + "'!");
        event.getEntity().setFallDistance(0);
        event.getEntity().setVelocity(exitEvent.getAfter());
    }

    @EventHandler
    // This event is fired when a player is teleported by a portal
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event instanceof PlayerPortalsPlayerPortalEvent) {
            // Ignore events generated by this plugin
            return;
        }

        if (!this.tracker.canEnterPortal(event.getPlayer())) {
            // We teleport the player as soon as they touch a valid portal, so the only cases where the player would stay in
            //  contact with one of our portals long enough to activate the standard portal behavior is if the destination is
            //  invalid or they just came out of the portal.  Cancel the event so they don't end up in the nether in these cases.
            event.setCancelled(true);
        }
    }

    @EventHandler
    // This event is fired when an entity other than a player is teleported by a portal
    public void onEntityPortal(EntityPortalEvent event) {
        if (event instanceof PlayerPortalsEntityPortalEvent) {
            return;
        }

        if (!this.tracker.canEnterPortal(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    //endregion

    //region Helpers

    private boolean sendPortalEvent(Portal originPortal, Portal destinationPortal, Entity entity, Location from, Location to) {
        // Send further portal events so other plugins can see
        if (entity instanceof Player) {
            PlayerPortalsPlayerPortalEvent teleportEvent = new PlayerPortalsPlayerPortalEvent(
                originPortal, destinationPortal, (Player) entity, from, to);

            Bukkit.getServer().getPluginManager().callEvent(teleportEvent);

            return teleportEvent.isCancelled();
        } else {
            PlayerPortalsEntityPortalEvent teleportEvent = new PlayerPortalsEntityPortalEvent(
                originPortal, destinationPortal, entity, from, to);

            Bukkit.getServer().getPluginManager().callEvent(teleportEvent);

            return teleportEvent.isCancelled();
        }
    }

    //endregion
}
