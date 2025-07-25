package me.bytebase.byteclient.features.modules.render;

import com.google.common.eventbus.Subscribe;
import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.event.impl.ClientEvent;
import me.bytebase.byteclient.features.commands.Command;
import me.bytebase.byteclient.features.gui.ByteClientGui;
import me.bytebase.byteclient.features.modules.Module;
import me.bytebase.byteclient.features.settings.Setting;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ClickGui
        extends Module {
    private static ClickGui INSTANCE = new ClickGui();
    public Setting<String> prefix = str("Prefix", ".");
    public Setting<Integer> red = num("Red", 0, 0, 255);
    public Setting<Integer> green = num("Green", 0, 0, 255);
    public Setting<Integer> blue = num("Blue", 255, 0, 255);
    public Setting<Integer> hoverAlpha = num("Alpha", 180, 0, 255);
    public Setting<Integer> topRed = num("SecondRed", 0, 0, 255);
    public Setting<Integer> topGreen = num("SecondGreen", 0, 0, 255);
    public Setting<Integer> topBlue = num("SecondBlue", 150, 0, 255);
    public Setting<Integer> alpha = num("HoverAlpha", 240, 0, 255);
    public Setting<Boolean> rainbow = bool("Rainbow", false);
    public Setting<Integer> rainbowHue = num("Delay", 240, 0, 600);
    public Setting<Float> rainbowBrightness = num("Brightness ", 150.0f, 1.0f, 255.0f);
    public Setting<Float> rainbowSaturation = num("Saturation", 150.0f, 1.0f, 255.0f);
    private ByteClientGui click;

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Module.Category.RENDER, true, false, false);
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        rainbowHue.setVisibility(v -> rainbow.getValue());
        rainbowBrightness.setVisibility(v -> rainbow.getValue());
        rainbowSaturation.setVisibility(v -> rainbow.getValue());
        this.setInstance();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                ByteClient.commandManager.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to " + Formatting.DARK_GRAY + ByteClient.commandManager.getPrefix());
            }
            ByteClient.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.hoverAlpha.getPlannedValue());
        }
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        mc.setScreen(ByteClientGui.getClickGui());
    }

    @Override
    public void onLoad() {
        ByteClient.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.hoverAlpha.getValue());
        ByteClient.commandManager.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGui.mc.currentScreen instanceof ByteClientGui)) {
            this.disable();
        }
    }

    public enum rainbowModeArray {
        Static,
        Up

    }

    public enum rainbowMode {
        Static,
        Sideway

    }
}