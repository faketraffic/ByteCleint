package me.bytebase.byteclient.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.event.impl.UpdateEvent;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.features.settings.Setting;
import me.bytebase.byteclient.util.models.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/**
 * @author Tharmsy
 * @since 07/9/2025
 */
public class MiddleClickPearl extends Module {

    private final Setting<Boolean> swap = this.register(new Setting<>("Swap", true));
    private final Setting<Integer> delay = this.register(new Setting<>("Delay", 200, 0, 1000));

    private boolean middleMouseDown = false;
    private final Timer timer = new Timer();

    public MiddleClickPearl() {
        super("MiddleClickPearl", "Throws a pearl on middle click.", Module.Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }


        if (GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS) {
            if (!this.middleMouseDown) {
                this.middleMouseDown = true;
                throwPearl();
            }
        } else {
            this.middleMouseDown = false;
        }
    }

    private void throwPearl() {
        if (swap.getValue()) {
            int pearlSlot = findPearlSlot();
            if (pearlSlot != -1) {
                int oldSlot = mc.player.getInventory().getSelectedSlot();

                mc.player.getInventory().setSelectedSlot(pearlSlot);

                // test
                timer.reset();
                new Thread(() -> {
                    while (!timer.passedMs(delay.getValue())) { }
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.player.getInventory().setSelectedSlot(oldSlot);
                }).start();
            }
        } else {
            // fix
            if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        }
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }
}
