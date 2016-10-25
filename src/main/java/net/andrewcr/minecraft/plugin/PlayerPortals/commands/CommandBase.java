package net.andrewcr.minecraft.plugin.PlayerPortals.commands;


import org.bukkit.command.CommandSender;

public abstract class CommandBase implements ICommand {
    public abstract CommandExecutorBase getExecutor();

    public final String getCommandName() {
        return this.getExecutor().getName();
    }

    public final boolean invoke(CommandSender sender, String[] args) {
        CommandExecutorBase executor = this.getExecutor();

        executor.init(sender, args);
        return executor.invoke();
    }
}
