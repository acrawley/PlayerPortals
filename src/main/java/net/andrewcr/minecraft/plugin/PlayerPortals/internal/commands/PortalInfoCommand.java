package net.andrewcr.minecraft.plugin.PlayerPortals.internal.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.Constants;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.PortalMessage;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.portals.PortalStore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PortalInfoCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new PortalInfoCommandExecutor();
    }

    private class PortalInfoCommandExecutor extends CommandExecutorBase {
        PortalInfoCommandExecutor() {
            super("portal info", Constants.PortalInfoPermission);
        }

        @Override
        protected boolean invoke(String[] args) {
            if (args.length != 1) {
                return false;
            }

            Portal portal = PortalStore.getInstance().getPortalByName(args[0]);
            if (portal == null) {
                this.error("Unknown portal '" + args[0] + "'!");
                return false;
            }

            this.sendMessage("Portal '" + portal.getName() + "':");
            this.sendMessage("  World: " + portal.getLocation().getWorld().getName());
            this.sendMessage("  Location: (" +
                portal.getLocation().getBlockX() + ", " +
                portal.getLocation().getBlockY() + ", " +
                portal.getLocation().getBlockZ() + ")");

            if (this.getPlayer() != null) {
                if (this.getPlayer().getWorld() == portal.getLocation().getWorld()) {
                    this.sendMessage("  Distance: " + this.getPlayer().getLocation().distance(portal.getLocation()) + " blocks");
                } else {
                    this.sendMessage("  Distance: (in another world)");
                }
            }

            if (!StringUtil.isNullOrEmpty(portal.getDestination())) {
                this.sendMessage("  Destination: " + portal.getDestination());
            }

            if (!StringUtil.isNullOrEmpty(portal.getMessage())) {
                this.sendMessage("  Message: " + portal.getMessage());
            }

            if (portal.getOwner() != null) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(portal.getOwner());
                this.sendMessage("  Owner: " + ((owner == null) ? "(unknown player)" : owner.getName()));
            }

            if (portal.getExitHeading() != null) {
                this.sendMessage("  Exit heading: Yaw = " + portal.getExitHeading().getYaw() +
                    "°, Pitch = " + portal.getExitHeading().getPitch() +
                    "°, Velocity" + (portal.getExitHeading().isAbsoluteVelocity() ? "" : " Multiplier") + " = " + portal.getExitHeading().getVelocityMultiplier());
            }

            PortalMessage message = portal.validatePortal();
            if (message == null) {
                this.sendMessage("  Status: " + ChatColor.GREEN + "OK");
            } else {
                this.sendMessage("  Status: " + (message.isError() ? ChatColor.RED : ChatColor.YELLOW) + message.getMessage());
            }

            return true;
        }
    }
}
