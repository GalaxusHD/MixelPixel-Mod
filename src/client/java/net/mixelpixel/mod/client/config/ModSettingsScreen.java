package net.mixelpixel.mod.client.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ModSettingsScreen extends Screen {
    private final Screen parent;
    public ModSettingsScreen(Screen parent) { super(Text.literal("MixelPixel Mod Einstellungen")); this.parent = parent; }
    @Override protected void init() {
        ModConfig c = ModConfig.get();
        int x = width / 2 - 110, y = Math.max(30, height / 2 - 90);
        addDrawableChild(ButtonWidget.builder(markerText(), button -> {
            c.markerState = switch (c.markerState) { case "on" -> "hidden"; case "hidden" -> "off"; default -> "on"; };
            c.save(); button.setMessage(markerText());
        }).dimensions(x, y, 220, 20).build());
        toggle("MSG", () -> c.messageEnabled, v -> c.messageEnabled = v, x, y + 25);
        toggle("/P KICK", () -> c.kickEnabled, v -> c.kickEnabled = v, x, y + 50);
        toggle("/P BAN", () -> c.banEnabled, v -> c.banEnabled = v, x, y + 75);
        toggle("REPORT", () -> c.reportEnabled, v -> c.reportEnabled = v, x, y + 100);
        toggle("NameMC", () -> c.nameMcEnabled, v -> c.nameMcEnabled = v, x, y + 125);
        addDrawableChild(new SelectionTimeoutSlider(x, y + 150));
    }
    private Text markerText() {
        String state = ModConfig.get().markerState;
        return Text.literal("Spielermarker: ").append(Text.literal(switch (state) {
            case "hidden" -> "Unsichtbar"; case "off" -> "Aus"; default -> "An";
        }).formatted(switch (state) {
            case "hidden" -> Formatting.YELLOW; case "off" -> Formatting.RED; default -> Formatting.GREEN;
        }));
    }
    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + ": ").append(Text.literal(enabled ? "Ja" : "Nein")
                .formatted(enabled ? Formatting.GREEN : Formatting.RED));
    }
    private void toggle(String label, BooleanSupplier getter, Consumer<Boolean> setter, int x, int y) {
        addDrawableChild(ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), button -> {
            setter.accept(!getter.getAsBoolean()); ModConfig.get().save();
            button.setMessage(toggleText(label, getter.getAsBoolean()));
        }).dimensions(x, y, 220, 20).build());
    }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
    }
    @Override public void close() { client.setScreen(parent); }
}

