package me.bytebase.byteclient.features.gui.items.buttons;

import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.features.gui.ByteClientGui;
import me.bytebase.byteclient.features.modules.render.ClickGui;
import me.bytebase.byteclient.features.settings.Setting;
import me.bytebase.byteclient.util.render.RenderUtil;
import me.bytebase.byteclient.util.models.Timer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class StringButton
        extends Button {
    private static final Timer idleTimer = new Timer();
    private static boolean idle;
    private final Setting<String> setting;
    public boolean isListening;
    private CurrentString currentString = new CurrentString("");

    public StringButton(Setting<String> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        RenderUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float) this.width + 7.4f, this.y + (float) this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? ByteClient.colorManager.getColorWithAlpha(ByteClient.moduleManager.getModuleByClass(ClickGui.class).hoverAlpha.getValue()) : ByteClient.colorManager.getColorWithAlpha(ByteClient.moduleManager.getModuleByClass(ClickGui.class).alpha.getValue())) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        if (this.isListening) {
            drawString(this.currentString.string() + "_", this.x + 2.3f, this.y - 1.7f - (float) ByteClientGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        } else {
            drawString((this.setting.getName().equals("Buttons") ? "Buttons " : (this.setting.getName().equals("Prefix") ? "Prefix  " + Formatting.GRAY : "")) + this.setting.getValue(), this.x + 2.3f, this.y - 1.7f - (float) ByteClientGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (this.isListening) {
            this.setString(this.currentString.string() + typedChar);
        }
    }

    @Override public void onKeyPressed(int key) {
        if (isListening) {
            switch (key) {
                case GLFW.GLFW_KEY_ENTER: {
                    this.enterString();
                }
                case GLFW.GLFW_KEY_BACKSPACE: {
                    this.setString(StringButton.removeLastChar(this.currentString.string()));
                }
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    private void enterString() {
        if (this.currentString.string().isEmpty()) {
            this.setting.setValue(this.setting.getDefaultValue());
        } else {
            this.setting.setValue(this.currentString.string());
        }
        this.setString("");
        this.onMouseClick();
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }

    public void setString(String newString) {
        this.currentString = new CurrentString(newString);
    }

    public static String getIdleSign() {
        if (idleTimer.passedMs(500)) {
            idle = !idle;
            idleTimer.reset();
        }
        if (idle) return "_";
        return "";
    }

    public record CurrentString(String string) {
    }
}