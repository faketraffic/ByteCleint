package me.bytebase.byteclient.manager;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.event.Stage;
import me.bytebase.byteclient.event.impl.*;
import me.bytebase.byteclient.features.Feature;
import me.bytebase.byteclient.features.commands.Command;
import me.bytebase.byteclient.util.models.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Formatting;

public class EventManager extends Feature {
    private final Timer logoutTimer = new Timer();

    public void init() {
        EVENT_BUS.register(this);
    }

    public void onUnload() {
        EVENT_BUS.unregister(this);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        mc.getWindow().setTitle("Byte client");
        if (!fullNullCheck()) {
//            ByteClient.inventoryManager.update();
            ByteClient.moduleManager.onUpdate();
            ByteClient.moduleManager.sortModules(true);
            onTick();
//            if ((HUD.getInstance()).renderingMode.getValue() == HUD.RenderingMode.Length) {
//                ByteClient.moduleManager.sortModules(true);
//            } else {
//                ByteClient.moduleManager.sortModulesABC();
//            }
        }
    }

    public void onTick() {
        if (fullNullCheck())
            return;
        ByteClient.moduleManager.onTick();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.getHealth() > 0.0F)
                continue;
            EVENT_BUS.post(new DeathEvent(player));
//            PopCounter.getInstance().onDeath(player);
        }
    }

    @Subscribe
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (fullNullCheck())
            return;
        if (event.getStage() == Stage.PRE) {
            ByteClient.speedManager.updateValues();
            ByteClient.rotationManager.updateRotations();
            ByteClient.positionManager.updatePosition();
        }
        if (event.getStage() == Stage.POST) {
            ByteClient.rotationManager.restoreRotations();
            ByteClient.positionManager.restorePosition();
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        ByteClient.serverManager.onPacketReceived();
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
            ByteClient.serverManager.update();
    }

    @Subscribe
    public void onWorldRender(Render3DEvent event) {
        ByteClient.moduleManager.onRender3D(event);
    }

    @Subscribe public void onRenderGameOverlayEvent(Render2DEvent event) {
        ByteClient.moduleManager.onRender2D(event);
    }

    @Subscribe public void onKeyInput(KeyEvent event) {
        ByteClient.moduleManager.onKeyPressed(event.getKey());
    }

    @Subscribe public void onChatSent(ChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.cancel();
            try {
                if (event.getMessage().length() > 1) {
                    ByteClient.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                } else {
                    Command.sendMessage("Please enter a command.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage(Formatting.RED + "An error occurred while running this command. Check the log!");
            }
        }
    }
}