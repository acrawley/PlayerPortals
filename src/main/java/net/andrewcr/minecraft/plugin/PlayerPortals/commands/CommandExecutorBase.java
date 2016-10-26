package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import net.andrewcr.minecraft.plugin.PlayerPortals.Plugin;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.ArrayUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CommandExecutorBase {
    //region Private Fields

    private CommandSender sender;
    private String[] args;
    private final String name;
    private final String permission;

    //endregion

    //region Constructors / Initialization

    public CommandExecutorBase(String name) {
        this(name, null);
    }

    public CommandExecutorBase(String name, String permission) {
        this.name = name;
        this.permission = permission;
    }

    public void init(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = args;
    }

    //endregion

    //region Abstract Methods

    protected abstract boolean invoke(String[] args);

    //endregion

    //region Helpers

    protected boolean allowConsole() {
        return true;
    }

    protected boolean isCommandGroup() {
        return false;
    }

    protected boolean isConsole() {
        return this.getPlayer() == null;
    }

    protected Player getPlayer() {
        if (this.sender instanceof Player) {
            return (Player) this.sender;
        }

        return null;
    }

    protected void sendMessage(String text) {
        this.sender.sendMessage(text);
    }

    protected void error(String text) {
        this.sendMessage(ChatColor.RED + text);
    }

    //endregion

    public String getName() { return this.name; }

    public boolean invoke() {
        // Check if allowed from console
        if (!this.allowConsole() && this.isConsole()) {
            this.error("The '" + this.name + "' command cannot be invoked from the console!");
            return true;
        }

        // Verify user has permission
        if (this.permission != null && this.getPlayer() != null && !this.getPlayer().hasPermission(this.permission)) {
            this.error("You do not have permission to invoke the '" + this.name + "' command!");
            return true;
        }

        // If the command is a group, try to find the subcommand and invoke that instead
        if (this.isCommandGroup() && this.args.length >= 1) {
            String subCommand = this.name + " " + this.args[0];

            Command cmd = Plugin.getInstance().getCommand(subCommand);
            if (cmd != null) {
                // Send args minus the subcommand
                cmd.execute(this.sender, subCommand, ArrayUtil.removeFirst(args, 1));

                // Don't print usage for the group
                return true;
            }
        }

        // Invoke the command
        return this.invoke(this.args);
    }
}
