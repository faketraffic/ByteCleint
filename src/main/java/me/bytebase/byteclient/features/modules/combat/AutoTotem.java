package me.bytebase.byteclient.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.event.impl.UpdateEvent;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.features.settings.Setting;
import me.bytebase.byteclient.util.models.Timer;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * @author Tharmsy
 * @since 07/9/2025
 */
public class AutoTotem extends Module {

    private final Setting<Integer> delay = this.register(new Setting<>("Delay", 100, 0, 1000));
    private final Timer timer = new Timer();

    public AutoTotem() {
        super("AutoTotem", "Automatically equips a totem when one pops.", Module.Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        if (!timer.passedMs(delay.getValue())) return;

        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        timer.reset();
    }

    private int findTotemSlot() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }
}
