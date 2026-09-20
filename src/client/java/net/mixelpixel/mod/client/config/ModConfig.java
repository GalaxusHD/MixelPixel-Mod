package net.mixelpixel.mod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mixelpixel-mod.json");
    private static ModConfig instance;

    public String targetGlowMode = "overlay";
    public String targetGlowColor = "white";
    public double targetRange = 32.0;
    public boolean targetMustBeVisible = true;
    public String markerState = "on";
    public boolean messageEnabled = true;
    public boolean kickEnabled = true;
    public boolean banEnabled = true;
    public boolean reportEnabled = true;
    public boolean nameMcEnabled = true;
    public int selectionTimeoutSeconds = 30;
    public int selectionSeconds() { return net.mixelpixel.mod.client.target.TargetLease.clampSeconds(selectionTimeoutSeconds); }

    public boolean targetingEnabled() { return !"off".equals(markerState); }
    public boolean markerVisible() { return "on".equals(markerState); }

    public static ModConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                instance = GSON.fromJson(Files.readString(FILE), ModConfig.class);
            }
        } catch (Exception ignored) {
            instance = null;
        }
        if (instance == null) instance = new ModConfig();
        if (!java.util.List.of("on", "hidden", "off").contains(instance.markerState == null ? "" : instance.markerState)) instance.markerState = "on";
        instance.save();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }

    public int glowRgb() {
        String value = targetGlowColor == null ? "white" : targetGlowColor.trim();
        if (value.startsWith("#")) {
            try {
                return Integer.parseInt(value.substring(1), 16) & 0xFFFFFF;
            } catch (NumberFormatException ignored) {
                return 0xFFFFFF;
            }
        }
        Formatting formatting = Formatting.byName(value.toLowerCase(Locale.ROOT));
        Integer color = formatting == null ? null : formatting.getColorValue();
        return color == null ? 0xFFFFFF : color;
    }
}

