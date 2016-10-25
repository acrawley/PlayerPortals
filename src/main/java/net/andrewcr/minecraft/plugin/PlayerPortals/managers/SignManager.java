package net.andrewcr.minecraft.plugin.PlayerPortals.managers;

import net.andrewcr.minecraft.plugin.PlayerPortals.Constants;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalMessage;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.LocationUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignManager implements Listener {
    //region Event Handlers

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChanged(SignChangeEvent event) {
        if (!event.getLine(0).equalsIgnoreCase("[portal]")) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission(Constants.CreatePortalPermission)) {
            SignManager.setSignLineColor(event, 0, ChatColor.RED);
            player.sendMessage(ChatColor.RED + "You do not have permission to create portals!");
            return;
        }

        // Get location, and set yaw based on sign orientation
        Location portalLoc = event.getBlock().getLocation();

        Portal portal = new Portal(
            portalLoc,
            LocationUtil.getSignAngle((Sign)event.getBlock().getState()),
            event.getPlayer().getUniqueId(),
            event.getLine(1),
            event.getLine(2),
            event.getLine(3));

        PortalMessage message = portal.validatePortal();
        if (message == null) {
            SignManager.setSignLineColor(event, 0, ChatColor.GREEN);
        } else {
            ChatColor color = (message.isError()) ? ChatColor.RED : ChatColor.YELLOW;

            SignManager.setSignLineColor(event, 0, color);
            player.sendMessage(color + message.getMessage());

            if (message.isError()) {
                return;
            }
        }

        PortalStore.getInstance().addPortal(portal);

        event.getPlayer().sendMessage("Created a new portal" +
            (!StringUtil.isNullOrEmpty(portal.getName()) ? " '" + portal.getName() + "'" : "") +
            (!StringUtil.isNullOrEmpty(portal.getDestination()) ? " to '" + portal.getDestination() + "'" : "") +
            "!");

        this.updateSignsAfterChange(portal);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) {
            // Not a sign, don't care
            return;
        }

        Location signLocation = event.getBlock().getLocation();
        Portal portal = PortalStore.getInstance().getPortalByLocation(signLocation);
        if (portal == null) {
            // Not a portal sign, don't care
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission(Constants.ModifyOwnPortalPermission)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to remove portals!");
            return;
        }

        if (portal.getOwner() != null &&
            portal.getOwner() != player.getUniqueId() &&
            !player.hasPermission(Constants.ModifyOtherPortalPermission)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to remove portals owned by another player!");
            return;
        }

        PortalStore.getInstance().removePortal(portal);

        player.sendMessage("Removed portal" +
            (!StringUtil.isNullOrEmpty(portal.getName()) ? " '" + portal.getName() + "'" : "") +
            "!");

        this.updateSignsAfterChange(portal);
    }

    //endregion

    //region Utilities

    private void updateSignsAfterChange(Portal changedPortal) {
        if (StringUtil.isNullOrEmpty(changedPortal.getName())) {
            // Portal wasn't named, so nothing could have targeted it
            return;
        }

        // Update signs on portals that targeted the portal that changed
        SignManager.updateSigns(PortalStore.getInstance().getPortalsByDestination(changedPortal.getName()));
    }

    public static void updateSigns(Iterable<Portal> portals) {
        for (Portal portal : portals) {
            if (!(portal.getLocation().getBlock().getState() instanceof Sign)) {
                continue;
            }

            Sign portalSign = (Sign)portal.getLocation().getBlock().getState();
            PortalMessage error = portal.validatePortal();
            ChatColor color;

            if (error == null) {
                color = ChatColor.GREEN;
            } else if (error.isError()) {
                color = ChatColor.RED;
            } else {
                color = ChatColor.YELLOW;
            }

            SignManager.setSignLineColor(portalSign, 0, color);
        }
    }

    private static void setSignLineColor(SignChangeEvent event, int line, ChatColor color) {
        event.setLine(line, color + ChatColor.stripColor(event.getLine(line)));
    }

    private static void setSignLineColor(Sign sign, int line, ChatColor color) {
        sign.setLine(line, color + ChatColor.stripColor(sign.getLine(line)));
        sign.update();
    }

    //endregion
}
