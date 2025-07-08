package me.bytebase.byteclient.features.commands.impl;

import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.features.commands.Command;
import net.minecraft.util.Formatting;

public class PrefixCommand
        extends Command {
    public PrefixCommand() {
        super("prefix", new String[]{"<char>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage(Formatting.GREEN + "Current prefix is " + ByteClient.commandManager.getPrefix());
            return;
        }
        ByteClient.commandManager.setPrefix(commands[0]);
        Command.sendMessage("Prefix changed to " + Formatting.GRAY + commands[0]);
    }
}