package me.bytebase.byteclient.features.modules.client;

import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.event.impl.Render2DEvent;
import me.bytebase.byteclient.features.modules.Module;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudModule extends Module {
    public HudModule() {
        super("Hud", "hud", Category.CLIENT, true, false, false);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        int screenWidth = mc.getWindow().getScaledWidth();

        event.getContext().drawTextWithShadow(
                mc.textRenderer,
                ByteClient.NAME + " " + ByteClient.VERSION,
                2, 2,
                -1
        );

        int y = 2;

        List<Module> sortedModules = ByteClient.moduleManager.getEnabledModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparing((Module m) -> mc.textRenderer.getWidth(m.getName())).reversed())
                .collect(Collectors.toList());

        for (Module module : sortedModules) {
            String name = module.getName();
            int textWidth = mc.textRenderer.getWidth(name);
            int x = screenWidth - textWidth - 4;

            event.getContext().fill(
                    x - 2,
                    y - 1,
                    x + textWidth + 2,
                    y + 9,
                    0x90000000
            );

            event.getContext().drawTextWithShadow(
                    mc.textRenderer,
                    name,
                    x,
                    y,
                    -1
            );

            y += 10;
        }
    }
}
