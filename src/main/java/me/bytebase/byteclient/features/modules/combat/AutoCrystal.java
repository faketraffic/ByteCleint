package me.bytebase.byteclient.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.event.impl.Render3DEvent;
import me.bytebase.byteclient.event.impl.UpdateEvent;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.features.settings.Setting;
import me.bytebase.byteclient.util.models.Timer;
import me.bytebase.byteclient.util.render.RenderUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;

import java.awt.Color;
import java.util.Comparator;
import java.util.stream.StreamSupport;

/**
 * @author Tharmsy
 * @since 07/9/2025
 */
public class AutoCrystal extends Module {

    private final Setting<Float> breakRange = this.register(new Setting<>("BreakRange", 4.5f, 1.0f, 6.0f));
    private final Setting<Integer> breakDelay = this.register(new Setting<>("BreakDelay", 1, 0, 10));
    private final Setting<Boolean> esp = this.register(new Setting<>("ESP", true));
    private final Setting<Boolean> placeObsidian = this.register(new Setting<>("PlaceObsidian", true));


    private final Timer breakTimer = new Timer();
    private EndCrystalEntity targetCrystal;
    private boolean wasUsePressed = false;

    public AutoCrystal() {
        super("AutoCrystal", "Automatically breaks End Crystals.", Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        if (placeObsidian.getValue()) {
            handleObsidianPlacement();
        }

        // test
        if (breakTimer.passedMs(breakDelay.getValue() * 50L)) {
            breakCrystal();
            breakTimer.reset();
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        // fix
        if (esp.getValue() && targetCrystal != null && !targetCrystal.isRemoved()) {
            Box box = targetCrystal.getBoundingBox();
            // fix
            RenderUtil.drawBox(event.getMatrix(), box, new Color(255, 0, 0, 150), 1.5f);
        }
    }

    private void handleObsidianPlacement() {
        boolean isUsePressed = mc.options.useKey.isPressed();
        if (isUsePressed && !wasUsePressed && mc.player.getMainHandStack().getItem() == Items.OBSIDIAN && mc.crosshairTarget instanceof BlockHitResult) {
            int crystalSlot = findHotbarSlot(Items.END_CRYSTAL);
            if (crystalSlot != -1) {
                new Thread(() -> {
                    try {
                        Thread.sleep(50);
                        mc.player.getInventory().setSelectedSlot(crystalSlot);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        wasUsePressed = isUsePressed;
    }

    private int findHotbarSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private void breakCrystal() {
        targetCrystal = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof EndCrystalEntity)
                .map(e -> (EndCrystalEntity) e)
                .filter(c -> mc.player.distanceTo(c) <= breakRange.getValue())
                .min(Comparator.comparing(c -> mc.player.distanceTo(c)))
                .orElse(null);

        if (targetCrystal != null) {
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(targetCrystal, mc.player.isSneaking()));
        }
    }
}
