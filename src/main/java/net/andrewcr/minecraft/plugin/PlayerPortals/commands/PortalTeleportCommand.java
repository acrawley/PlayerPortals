package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.PlayerPortals.Constants;
import net.andrewcr.minecraft.plugin.PlayerPortals.listeners.PortalDestination;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PortalTeleportCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new PortalTeleportCommandExecutor();
    }

    private class PortalTeleportCommandExecutor extends CommandExecutorBase {
        PortalTeleportCommandExecutor() {
            super("ptp", Constants.TeleportCommandPermission);
        }

        @Override
        protected boolean invoke(String[] args) {
            Player player;
            String destinationName;

            if (args.length == 1) {
                player = this.getPlayer();

                if (player == null) {
                    this.error("Console invocation must specify a player!");
                    return false;
                }

                destinationName = args[0];
            } else if (args.length == 2) {
                player = Bukkit.getPlayer(args[0]);

                if (player == null) {
                    this.error("Unknown player '" + args[0] + "'!");
                    return false;
                }

                destinationName = args[1];
            } else {
                return false;
            }

            PortalDestination destination = PortalDestination.getPortalDestinationByName(destinationName, player);
            if (destination == null) {
                error("Unknown destination '" + args[0] + "'!");
                return false;
            }

            player.teleport(destination.getLocation());
            player.sendMessage("You warped to '" + destination.getDescription() + "'!");

            return true;
        }
    }
}
