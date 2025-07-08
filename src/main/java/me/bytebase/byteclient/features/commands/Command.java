package me.bytebase.byteclient.features.commands;

import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.features.Feature;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public abstract class Command
        extends Feature {
    protected String name;
    protected String[] commands;

    public Command(String name) {
        super(name);
        this.name = name;
        this.commands = new String[]{""};
    }

    public Command(String name, String[] commands) {
        super(name);
        this.name = name;
        this.commands = commands;
    }

    public static void sendMessage(String message) {
        Command.sendSilentMessage(ByteClient.commandManager.getClientMessage() + " " + Formatting.GRAY + message);
    }

    public static void sendSilentMessage(String message) {
        if (Command.nullCheck()) {
            return;
        }
        // TODO add silent support ig
        mc.inGameHud.getChatHud().addMessage(Text.literal(message));
    }

    public static String getCommandPrefix() {
        return ByteClient.commandManager.getPrefix();
    }

    public abstract void execute(String[] var1);

    @Override
    public String getName() {
        return this.name;
    }

    public String[] getCommands() {
        return this.commands;
    }
}