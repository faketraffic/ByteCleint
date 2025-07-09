package me.bytebase.byteclient.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.event.impl.Render3DEvent;
import me.bytebase.byteclient.event.impl.UpdateEvent;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.features.settings.Setting;
import me.bytebase.byteclient.util.models.Timer;
import me.bytebase.byteclient.util.render.RenderUtil;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
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
    private final Setting<Color> espColor = this.register(new Setting<>("ESP Color", new Color(255, 0, 0, 150)));


    private final Timer breakTimer = new Timer();
    private EndCrystalEntity targetCrystal;

    public AutoCrystal() {
        super("AutoCrystal", "Automatically breaks End Crystals.", Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
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
            RenderUtil.drawBox(event.getMatrix(), box, espColor.getValue(), 1.5f);
        }
    }

    private void breakCrystal() {
        targetCrystal = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof EndCrystalEntity)
                .map(e -> (EndCrystalEntity) e)
                .filter(c -> mc.player.distanceTo(c) <= breakRange.getValue())
                .min(Comparator.comparing(c -> mc.player.distanceTo(c)))
                .orElse(null);

        // trying this shit
        if (targetCrystal != null) {
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(targetCrystal, mc.player.isSneaking()));
        }
    }
}
