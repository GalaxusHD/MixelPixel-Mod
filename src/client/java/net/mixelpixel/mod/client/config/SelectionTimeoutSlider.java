package net.mixelpixel.mod.client.config;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class SelectionTimeoutSlider extends SliderWidget {
    public SelectionTimeoutSlider(int x, int y) {
        super(x, y, 220, 20, Text.empty(), (ModConfig.get().selectionSeconds() - 3) / 117.0);
        updateMessage();
    }
    private int seconds() { return 3 + (int) Math.round(value * 117); }
    @Override protected void updateMessage() {
        setMessage(Text.literal("Auswahl lösen nach: " + seconds() + " s"));
    }
    @Override protected void applyValue() {
        ModConfig.get().selectionTimeoutSeconds = seconds();
        ModConfig.get().save();
    }
}
