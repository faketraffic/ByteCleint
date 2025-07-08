package me.bytebase.byteclient.features.commands.impl;

import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.features.commands.Command;
import me.bytebase.byteclient.features.modules.Module;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", new String[] {"<module>"});
    }

    @Override public void execute(String[] var1) {
        if (var1.length < 1 || var1[0] == null) {
            notFound();
            return;
        }
        Module mod = ByteClient.moduleManager.getModuleByName(var1[0]);
        if (mod == null) {
            notFound();
            return;
        }
        mod.toggle();
    }

    private void notFound() {
        sendMessage("Module is not found.");
    }
}
