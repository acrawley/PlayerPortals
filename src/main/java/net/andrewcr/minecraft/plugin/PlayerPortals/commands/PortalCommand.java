package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.GroupCommandExecutorBase;

public class PortalCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new PortalCommandExecutor();
    }

    private class PortalCommandExecutor extends GroupCommandExecutorBase {
        public PortalCommandExecutor() {
            super("portal");
        }
    }
}
