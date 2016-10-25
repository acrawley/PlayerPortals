package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import org.bukkit.command.CommandSender;

public interface ICommand {
    String getCommandName();
    boolean invoke(CommandSender sender, String[] args);
}
