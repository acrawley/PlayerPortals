package net.andrewcr.minecraft.plugin.PlayerPortals.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;

public class PortalCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new PortalCommandExecutor();
    }

    private class PortalCommandExecutor extends CommandExecutorBase {
        public PortalCommandExecutor() {
            super("portal");
        }

        @Override
        protected boolean isCommandGroup() {
            return true;
        }

        protected boolean invoke(String[] args) {
            // Command is a container for other commands, so just show usage if invoked directly
            return false;
        }
    }
}
