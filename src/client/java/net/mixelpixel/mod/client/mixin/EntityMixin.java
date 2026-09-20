package net.mixelpixel.mod.client.mixin;

import net.minecraft.entity.Entity;
import net.mixelpixel.mod.client.config.ModConfig;
import net.mixelpixel.mod.client.target.PlayerTargeting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces a server team/glow color only for the player currently selected by this client. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void mixelpixel$targetGlowColor(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == PlayerTargeting.getMarkedTarget()) {
            cir.setReturnValue(ModConfig.get().glowRgb());
        }
    }
}

