package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import net.andrewcr.minecraft.plugin.PlayerPortals.Constants;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalExitHeading;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.ArrayUtil;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class PortalSetCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new PortalSetCommandExecutor();
    }

    private class PortalSetCommandExecutor extends CommandExecutorBase {
        public PortalSetCommandExecutor() {
            super("portal set", Constants.PortalSetPermission);
        }

        @Override
        protected boolean invoke(String[] args) {
            if (args.length < 2) {
                return false;
            }

            Portal portal = PortalStore.getInstance().getPortalByName(args[0]);
            if (portal == null) {
                this.error("Unknown portal '" + portal + "'!");
                return false;
            }

            if (this.getPlayer() != null) {
                if ((portal.getOwner() == null || portal.getOwner().equals(this.getPlayer().getUniqueId())) && !this.getPlayer().hasPermission(Constants.ModifyOwnPortalPermission)) {
                    this.error("You do not have permission to modify this portal!");
                    return true;
                }

                if (portal.getOwner() != null && !portal.getOwner().equals(this.getPlayer().getUniqueId()) && !this.getPlayer().hasPermission(Constants.ModifyOtherPortalPermission)) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(portal.getOwner());
                    if (player == null) {
                        this.error("You do not have permission to modify a portal belonging to another player!");
                    } else {
                        this.error("You do not have permission to modify a portal that belongs to '" + player.getName() + "'!");
                    }

                    return true;
                }
            }

            String property = args[1].toLowerCase();
            String[] propertyArgs = ArrayUtil.removeFirst(args, 2);

            switch (property) {
                case "owner":
                    return this.setOwner(portal, propertyArgs);

                case "description":
                    return this.setDescription(portal, propertyArgs);

                case "exitheading":
                    return this.setExitHeading(portal, propertyArgs);
            }

            this.error("Unknown property '" + property + "'!");
            return false;
        }

        private boolean setOwner(Portal portal, String[] args) {
            if (args.length != 1) {
                return false;
            }

            if (StringUtil.equalsIgnoreCase(args[0], "(none)")) {
                // Reset to unowned
                portal.setOwner(null);
                this.sendMessage(ChatColor.YELLOW + "Portal '" + portal.getName() + "' is now unowned!");
            } else {
                // Set owner
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    this.error("Unknown player '" + args[0] + "'!");
                    return false;
                }

                portal.setOwner(player.getUniqueId());
                this.sendMessage(ChatColor.YELLOW + "Portal '" + portal.getName() + "' is now owned by player '" + player.getName() + "'!");
            }

            return true;
        }

        private boolean setDescription(Portal portal, String[] args) {
            String description = String.join(" ", args);

            if (!(portal.getLocation().getBlock().getState() instanceof Sign)) {
                this.error("Portal sign missing?");
                return false;
            }

            // Update sign and portal data
            Sign portalSign = (Sign) portal.getLocation().getBlock().getState();
            portalSign.setLine(3, ChatColor.BLUE + ChatColor.stripColor(description));
            portalSign.update();

            portal.setDescription(description);

            this.sendMessage("Portal '" + portal.getName() + "' description set to '" + description + "'!");

            return true;
        }

        private boolean setExitHeading(Portal portal, String[] args) {
            if (args.length == 1) {
                if (StringUtil.equalsIgnoreCase(args[0], "clear")) {
                    portal.setExitHeading(null);
                    this.sendMessage("Portal '" + portal.getName() + "' exit vector cleared!");
                    return true;
                }
            } else if (args.length == 3 || args.length == 4) {
                float yaw;
                float pitch;
                float multiplier;
                boolean isAbsolute = args.length == 4 && StringUtil.equalsIgnoreCase(args[3], "abs");

                try {
                    yaw = Float.parseFloat(args[0]);
                    pitch = Float.parseFloat(args[1]);
                    multiplier = Float.parseFloat(args[2]);
                } catch (NumberFormatException ex) {
                    this.error(ex.getMessage());
                    return false;
                }

                if (yaw < -90 || yaw > 90) {
                    this.error("Yaw must be between -90 and 90 degrees!");
                    return false;
                }

                if (pitch < -90 || pitch > 90) {
                    this.error("Pitch must be between -90 and 90 degrees!");
                    return false;
                }

                if (multiplier < 0) {
                    this.error("Velocity multiplier must be 0 or greater!");
                    return false;
                }


                portal.setExitHeading(new PortalExitHeading(yaw, pitch, multiplier, isAbsolute));
                this.sendMessage("Portal '" + portal.getName() + "' exit heading set - pitch = " + pitch +
                    ", yaw = " + yaw +
                    ", velocity" + (!isAbsolute ? " multiplier" : "") + " = " + multiplier + "!");

                return true;
            }

            return false;
        }
    }
}
