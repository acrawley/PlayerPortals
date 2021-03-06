package net.andrewcr.minecraft.plugin.PlayerPortals.internal.listeners;

import net.andrewcr.minecraft.plugin.BasePluginLib.util.LocationUtil;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.Constants;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.integration.dynmap.DynmapIntegration;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.PortalMessage;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.PortalStore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;

public class SignListener implements Listener {
    //region Event Handlers

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChanged(SignChangeEvent event) {
        if (!event.getLine(0).equalsIgnoreCase("[portal]")) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission(Constants.CreatePortalPermission)) {
            SignListener.setSignLineColor(event, 0, ChatColor.RED);
            player.sendMessage(ChatColor.RED + "You do not have permission to create portals!");
            return;
        }

        // Get location, and set yaw based on sign orientation
        Location portalLoc = event.getBlock().getLocation();

        Portal portal = new Portal(
            portalLoc,
            LocationUtil.getSignAngle((Sign) event.getBlock().getState()),
            event.getPlayer().getUniqueId(),
            event.getLine(1),
            event.getLine(2),
            event.getLine(3));

        PortalMessage message = portal.validatePortal();
        if (message == null) {
            SignListener.setSignLineColor(event, 0, ChatColor.GREEN);
        } else {
            ChatColor color = (message.isError()) ? ChatColor.RED : ChatColor.YELLOW;

            SignListener.setSignLineColor(event, 0, color);
            player.sendMessage(color + message.getMessage());

            if (message.isError()) {
                return;
            }
        }

        PortalStore.getInstance().addPortal(portal);
        DynmapIntegration.getInstance().notifyPortalCreated(portal);

        event.getPlayer().sendMessage("Created a new portal" +
            (!StringUtil.isNullOrEmpty(portal.getName()) ? " '" + portal.getName() + "'" : "") +
            (!StringUtil.isNullOrEmpty(portal.getDestination()) ? " to '" + portal.getDestination() + "'" : "") +
            "!");

        this.updateSignsAfterChange(portal);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getState() instanceof Sign) {
            // Broken block was a sign
            this.handleSignBreak(event, block);
            return;
        }

        Block attachedSign = Arrays.stream(new BlockFace[] { BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
            .map(f -> block.getRelative(f))
            .filter(b -> b.getState() instanceof Sign)
            .findAny()
            .orElse(null);

        if (attachedSign != null) {
            // Broken block had a sign attached
            this.handleSignBreak(event, attachedSign);
            return;
        }
    }

    private void handleSignBreak(BlockBreakEvent event, Block block) {
        Location signLocation = block.getLocation();
        Portal portal = PortalStore.getInstance().getPortalByLocation(signLocation);
        if (portal == null) {
            // Not a portal sign, don't care
            return;
        }

        Player player = event.getPlayer();
        if (portal.getOwner() != null &&
            portal.getOwner() == player.getUniqueId() &&
            !player.hasPermission(Constants.ModifyOwnPortalPermission)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to remove portals!");
            event.setCancelled(true);
            return;
        }

        if (portal.getOwner() != null &&
            portal.getOwner() != player.getUniqueId() &&
            !player.hasPermission(Constants.ModifyOtherPortalPermission)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to remove portals owned by another player!");
            event.setCancelled(true);
            return;
        }

        PortalStore.getInstance().removePortal(portal);
        DynmapIntegration.getInstance().notifyPortalRemoved(portal);

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
        SignListener.updateSigns(PortalStore.getInstance().getPortalsByDestination(changedPortal.getName()));
    }

    public static void updateSigns(Iterable<Portal> portals) {
        for (Portal portal : portals) {
            if (!(portal.getLocation().getBlock().getState() instanceof Sign)) {
                continue;
            }

            Sign portalSign = (Sign) portal.getLocation().getBlock().getState();
            PortalMessage error = portal.validatePortal();
            ChatColor color;

            if (error == null) {
                color = ChatColor.GREEN;
            } else if (error.isError()) {
                color = ChatColor.RED;
            } else {
                color = ChatColor.YELLOW;
            }

            SignListener.setSignLineColor(portalSign, 0, color);
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
