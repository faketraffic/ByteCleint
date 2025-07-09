package me.bytebase.byteclient.features.modules.misc;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.event.impl.PacketEvent;
import me.bytebase.byteclient.event.impl.UpdateEvent;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.util.models.Timer;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * @author Tharmsy
 * @since 07/9/2025
 */
public class AutoFish extends Module {
    private final Timer timer = new Timer();
    private boolean shouldRecast = false;
    public AutoFish() {
        super("AutoFish", "Automatically fishes for you.", Module.Category.MISC, true, false, false);
    }
    @Override
    public void onEnable() {
        super.onEnable();
        shouldRecast = true;
        timer.reset();
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mc.player.getMainHandStack().getItem() != Items.FISHING_ROD && mc.player.getOffHandStack().getItem() != Items.FISHING_ROD) {
            return;
        }
        if (shouldRecast && mc.player.fishHook == null && timer.passedMs(500)) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            shouldRecast = false;
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.player.fishHook == null) {
            return;
        }
        if (event.getPacket() instanceof PlaySoundS2CPacket) {
            PlaySoundS2CPacket packet = (PlaySoundS2CPacket) event.getPacket();
            if (packet.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
                if (mc.player.fishHook.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 1.0) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    shouldRecast = true;
                    timer.reset();
                }
            }
        }
    }
}
