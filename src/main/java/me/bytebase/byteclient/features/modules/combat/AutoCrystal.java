package me.bytebase.byteclient.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.event.impl.UpdateEvent;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.features.settings.Setting;
import me.bytebase.byteclient.util.models.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Tharmsy
 * @since 07/9/2025
 */
public class AutoCrystal extends Module {
// todo fix placeing not needed rn
    private final Setting<Boolean> place = this.register(new Setting<>("Place", true));
    private final Setting<Boolean> breakCrystals = this.register(new Setting<>("Break", true));
    private final Setting<Float> placeRange = this.register(new Setting<>("PlaceRange", 4.5f, 1.0f, 6.0f));
    private final Setting<Float> breakRange = this.register(new Setting<>("BreakRange", 4.5f, 1.0f, 6.0f));
    private final Setting<Float> targetRange = this.register(new Setting<>("TargetRange", 8.0f, 1.0f, 12.0f));
    private final Setting<Integer> placeDelay = this.register(new Setting<>("PlaceDelay", 1, 0, 10));
    private final Setting<Integer> breakDelay = this.register(new Setting<>("BreakDelay", 1, 0, 10));

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();

    public AutoCrystal() {
        super("AutoCrystal", "Test Module.", Category.COMBAT, true, false, false);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null || mc.player.isUsingItem()) {
            return;
        }

        if (breakCrystals.getValue() && breakTimer.passedMs(breakDelay.getValue() * 50L)) {
            if (breakCrystal()) {
                breakTimer.reset();
                return;
            }
        }

        if (place.getValue() && placeTimer.passedMs(placeDelay.getValue() * 50L)) {
            if (placeCrystal()) {
                placeTimer.reset();
            }
        }
    }

    private boolean breakCrystal() {
        EndCrystalEntity crystalToBreak = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(entity -> entity instanceof EndCrystalEntity)
                .map(entity -> (EndCrystalEntity) entity)
                .filter(crystal -> mc.player.distanceTo(crystal) <= breakRange.getValue())
                .min(Comparator.comparing(crystal -> mc.player.distanceTo(crystal)))
                .orElse(null);

        if (crystalToBreak != null) {
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalToBreak, mc.player.isSneaking()));
            return true;
        }
        return false;
    }

    private boolean placeCrystal() {
        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(player -> player != mc.player && player.isAlive())
                .filter(player -> mc.player.distanceTo(player) <= targetRange.getValue())
                .min(Comparator.comparing(player -> mc.player.distanceTo(player)))
                .orElse(null);

        if (target == null) return false;

        BlockPos bestPos = findBestPlacePos(target);
        if (bestPos == null) return false;

        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(bestPos)) > 25.0) return false;

        ItemStack main = mc.player.getMainHandStack();
        ItemStack off = mc.player.getOffHandStack();

        Hand hand = null;
        if (main.getItem() == Items.END_CRYSTAL) hand = Hand.MAIN_HAND;
        else if (off.getItem() == Items.END_CRYSTAL) hand = Hand.OFF_HAND;

        if (hand != null) {
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(bestPos), Direction.UP, bestPos, false);
            ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hit);
            if (result.isAccepted()) {
                mc.player.swingHand(hand);
                return true;
            }
        }

        return false;
    }

// doesnt work
    private BlockPos findBestPlacePos(PlayerEntity target) {
        List<BlockPos> placeableBlocks = findPlaceableBlocks();
        return placeableBlocks.stream()
                .min(Comparator.comparing(pos -> Vec3d.of(pos).distanceTo(target.getPos())))
                .orElse(null);
    }

    private List<BlockPos> findPlaceableBlocks() {
        BlockPos playerPos = mc.player.getBlockPos();
        int range = placeRange.getValue().intValue();
        return StreamSupport.stream(BlockPos.iterate(
                        playerPos.add(-range, -range, -range),
                        playerPos.add(range, range, range)
                ).spliterator(), false)
                .map(BlockPos::toImmutable)
                .filter(this::canPlaceCrystal)
                .collect(Collectors.toList());
    }
// fix?
    private boolean canPlaceCrystal(BlockPos pos) {
        if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
            return false;
        }

        BlockPos crystalPos1 = pos.up();
        BlockPos crystalPos2 = pos.up(2);
        if (!mc.world.isAir(crystalPos1) || !mc.world.isAir(crystalPos2)) {
            return false; // set to true
        }

        Box checkZone = new Box(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
        return mc.world.getOtherEntities(null, checkZone).isEmpty();
    }
}
