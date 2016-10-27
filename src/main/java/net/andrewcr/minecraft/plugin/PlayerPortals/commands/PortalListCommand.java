package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.PlayerPortals.Constants;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PortalListCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new PortalListCommandExecutor();
    }

    private class PortalListCommandExecutor extends CommandExecutorBase {
        public PortalListCommandExecutor() {
            super("portal list", Constants.ListPortalsPermission);
        }

        protected boolean invoke(String[] args) {
            Iterable<Portal> portals = null;

            if (args.length == 0) {
                if (this.isConsole()) {
                    // Run from the console with no arguments - show all portals
                    portals = PortalStore.getInstance().getPortals();
                } else {
                    // Run by a player with no arguments - show portals in the current world
                    portals = PortalStore.getInstance().getPortalsInWorld(this.getPlayer().getWorld());
                }
            } else if (args.length == 1) {
                World world = Bukkit.getWorld(args[0]);
                if (world == null) {
                    this.sendMessage(ChatColor.RED + "Unknown world '" + args[0] + "'!");
                    return false;
                }

                // Show portals in the specified world
                portals = PortalStore.getInstance().getPortalsInWorld(world);
            } else {
                return false;
            }

            this.sendMessage("Portals: ");

            if (portals != null) {
                this.sendMessage(StreamSupport.stream(portals.spliterator(), false)
                    .filter(p -> !StringUtil.isNullOrEmpty(p.getName()))
                    .map(p -> p.getName())
                    .collect(Collectors.joining(", ")));
            }

            return true;
        }
    }
}
