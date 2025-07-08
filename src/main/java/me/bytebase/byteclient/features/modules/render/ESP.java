package me.bytebase.byteclient.features.modules.render;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.event.impl.Render3DEvent;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.awt.Color;

public class ESP extends Module {
    public ESP() {
        super("ESP", "Highlights players and other entities.", Category.RENDER, true, false, false);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player) {
                Box box = entity.getBoundingBox();
                RenderUtil.drawBox(event.getMatrix(), box, Color.ORANGE, 1f);
            }
        }
    }
}