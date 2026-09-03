package net.mixelpixel.mod.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.Text;
import net.mixelpixel.mod.client.config.ModConfig;

public final class MixelPixelModClient implements ClientModInitializer {
    public static final String MOD_ID = "mixelpixelmod";
    @Override
    public void onInitializeClient() {
        ModConfig.load();
    }

    public static Text versionText() {
        return Text.translatable("mixelpixelmod.version");
    }
}
