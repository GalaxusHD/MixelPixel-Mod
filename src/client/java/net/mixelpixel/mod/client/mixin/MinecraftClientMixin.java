package net.mixelpixel.mod.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.mixelpixel.mod.client.MixelPixelModClient;
import net.mixelpixel.mod.client.screen.EmptyRadialMenuScreen;
import net.mixelpixel.mod.client.target.PlayerTargeting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void mixelpixel$tick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        PlayerTargeting.tick(client);
        while (MixelPixelModClient.settingsKey.wasPressed()) {
            if (client.currentScreen == null && client.player != null) {
                client.setScreen(new net.mixelpixel.mod.client.config.ModSettingsScreen(null));
            }
        }
        // Drain presses on every server so none carry over into a later session.
        while (MixelPixelModClient.playerMenuKey.wasPressed()) {
            if (client.currentScreen == null && EmptyRadialMenuScreen.hasActions() && PlayerTargeting.getTarget() != null) {
                client.setScreen(new EmptyRadialMenuScreen(PlayerTargeting.getTarget()));
            }
        }
    }

    @Inject(method = "hasOutline", at = @At("RETURN"), cancellable = true)
    private void mixelpixel$forceTargetOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity == PlayerTargeting.getMarkedTarget()) cir.setReturnValue(true);
    }
}


