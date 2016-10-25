package net.andrewcr.minecraft.plugin.PlayerPortals.managers;

import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.LocationUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.StringUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class PortalManager implements Listener {
    //region Private Fields

    private PortalUsageTracker tracker;

    //endregion

    //region Constructor

    public PortalManager() {
        this.tracker = new PortalUsageTracker();
    }

    //endregion

    //region Custom Event Types

    private class CustomEntityPortalEvent extends EntityPortalEvent {
        public CustomEntityPortalEvent(Entity entity, Location from, Location to, TravelAgent pta) {
            super(entity, from, to, pta);
        }
    }

    private class CustomPlayerPortalEvent extends PlayerPortalEvent {
        public CustomPlayerPortalEvent(Player player, Location from, Location to, TravelAgent pta, TeleportCause cause) {
            super(player, from, to, pta, cause);
        }
    }

    //endregion

    //region Event Listeners

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.getEntity().sendMessage("whoops");
        }
    }

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
        PortalDestination destination = PortalDestination.getPortalDestination(enteredPortal.getDestination());
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
        if (this.sendPortalEvent(event.getEntity(), event.getEntity().getLocation(), destination.getLocation())) {
            // Teleport was cancelled
            return;
        }

        // Move the reference point to the exit location
        this.tracker.setReferencePoint(event.getEntity(), destination.getLocation());

        // Conserve momentum
        Vector velocityBefore = event.getEntity().getVelocity().clone();
        Vector velocityAfter = destination.getLocation().getDirection().multiply(velocityBefore.length());
        if (destination.getPortal().getExitHeading() != null) {
            if (destination.getPortal().getExitHeading().isAbsoluteVelocity()) {
                // Set velocity to the value in the heading
                velocityAfter = destination.getLocation().getDirection().multiply(destination.getPortal().getExitHeading().getVelocityMultiplier());
            }
            else {
                // Apply the portal's velocity multiplier
                velocityAfter = velocityAfter.multiply(destination.getPortal().getExitHeading().getVelocityMultiplier());
            }
        }

        EntityPortalExitEvent exitEvent = new EntityPortalExitEvent(
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
        if (event instanceof CustomPlayerPortalEvent) {
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
        if (event instanceof CustomEntityPortalEvent) {
            return;
        }

        if (!this.tracker.canEnterPortal(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    //endregion

    //region Helpers

    private boolean sendPortalEvent(Entity entity, Location from, Location to) {
        // Send further portal events so other plugins can see
        if (entity instanceof Player) {
            CustomPlayerPortalEvent teleportEvent = new CustomPlayerPortalEvent(
                (Player) entity, from, to, null, PlayerTeleportEvent.TeleportCause.PLUGIN);

            Bukkit.getServer().getPluginManager().callEvent(teleportEvent);

            return teleportEvent.isCancelled();
        } else {
            CustomEntityPortalEvent teleportEvent = new CustomEntityPortalEvent(
                entity, from, to, null);

            Bukkit.getServer().getPluginManager().callEvent(teleportEvent);

            return teleportEvent.isCancelled();
        }
    }

    //endregion
}
