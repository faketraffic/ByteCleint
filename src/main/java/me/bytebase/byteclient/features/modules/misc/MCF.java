package me.bytebase.byteclient.features.modules.misc;

import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.features.commands.Command;
import me.bytebase.byteclient.features.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class MCF extends Module {
    private boolean pressed;
    public MCF() {
        super("MCF", "Middle click friend", Category.MISC, true, false, false);
    }

    @Override public void onTick() {
        if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 2) == 1) {
            if (!pressed) {
                Entity targetedEntity = mc.targetedEntity;
                if (!(targetedEntity instanceof PlayerEntity)) return;
                String name = ((PlayerEntity) targetedEntity).getGameProfile().getName();

                if (ByteClient.friendManager.isFriend(name)) {
                    ByteClient.friendManager.removeFriend(name);
                    Command.sendMessage(Formatting.RED + name + Formatting.RED + " has been unfriended.");
                } else {
                    ByteClient.friendManager.addFriend(name);
                    Command.sendMessage(Formatting.AQUA + name + Formatting.AQUA + " has been friended.");
                }

                pressed = true;
            }
        } else {
            pressed = false;
        }
    }
}
