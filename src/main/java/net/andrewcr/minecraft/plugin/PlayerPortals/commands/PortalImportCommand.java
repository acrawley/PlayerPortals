package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import net.andrewcr.minecraft.plugin.PlayerPortals.Constants;
import net.andrewcr.minecraft.plugin.PlayerPortals.Plugin;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.Portal;
import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalStore;

import net.andrewcr.minecraft.plugin.PlayerPortals.model.portals.PortalMessage;
import net.andrewcr.minecraft.plugin.PlayerPortals.managers.SignManager;
import net.andrewcr.minecraft.plugin.PlayerPortals.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PortalImportCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new PortalImportCommandExecutor();
    }

    private class PortalImportCommandExecutor extends CommandExecutorBase {
        public PortalImportCommandExecutor() {
            super("portal import", Constants.ImportPortalsPermission);
        }

        @Override
        protected boolean invoke(String[] args) {
            if (args.length != 1) {
                return false;
            }

            Collection<Portal> importedPortals = null;

            String sourcePlugin = args[0].toLowerCase();
            switch (sourcePlugin)
            {
                case "myworlds":
                    importedPortals = this.importMyWorlds();
                    break;

                default:
                    this.sendMessage("Don't know how to import from plugin '" + sourcePlugin + "'!");
                    return false;
            }

            if (importedPortals != null) {
                this.sendMessage("Imported " + importedPortals.size() + " portal(s)");

                // Update the signs on the imported portals
                SignManager.updateSigns(importedPortals);
            }

            return true;
        }

        private Collection<Portal> importMyWorlds() {
            List<Portal> importedPortals = new ArrayList<>();

            File pluginFolder = Plugin.getInstance().getDataFolder().getParentFile();
            if (!pluginFolder.exists()) {
                this.error("Could not locate plugins folder!");
                return importedPortals;
            }

            File myWorldsFolder = new File(pluginFolder, "My_Worlds");
            if (!myWorldsFolder.exists()) {
                this.error("Could not locate MyWorlds folder!");
                return importedPortals;
            }

            File myWorldsPortalsFile = new File(myWorldsFolder, "portals.txt");
            if (!myWorldsPortalsFile.exists()) {
                this.error("Could not locate MyWorlds portal list!");
                return importedPortals;
            }

            try {
                BufferedReader reader = new BufferedReader(new FileReader(myWorldsPortalsFile));
                String line;

                while ((line = reader.readLine()) != null) {
                    try {
                        String[] parts = line.split(" ");

                        if (parts.length != 7) {
                            this.error("Invalid portal definition: " + line);
                            continue;
                        }

                        // Get location of portal sign
                        String worldName = parts[1].replace("\"", "");
                        World world = Bukkit.getWorld(worldName);

                        Location location = new Location(
                            world,
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]));

                        if (!(location.getBlock().getState() instanceof Sign)) {
                            this.error("No portal sign found at location (" + parts[2] + ", " + parts[3] + ", " + parts[4] + ")!");
                            continue;
                        }

                        // Create portal using information from sign
                        Sign portalSign = (Sign)location.getBlock().getState();
                        Portal portal = new Portal(
                            location,
                            LocationUtil.getSignAngle((Sign)location.getBlock().getState()),
                            null,
                            ChatColor.stripColor(portalSign.getLine(1)),
                            ChatColor.stripColor(portalSign.getLine(2)),
                            ChatColor.stripColor(portalSign.getLine(3)));

                        PortalMessage message = portal.validatePortal();
                        if (message != null && message.isError()) {
                            this.error("Cannot import portal - " + message.getMessage());
                            continue;
                        }

                        PortalStore.getInstance().addPortal(portal);
                        importedPortals.add(portal);
                    }
                    catch (Exception ex) {
                        this.error("Invalid portal definition: " + line);
                    }
                }
            }
            catch (Exception ex) {
                this.error("MyWorlds import failed!");
                this.error(ex.toString());
            }

            return importedPortals;
        }
    }
}
