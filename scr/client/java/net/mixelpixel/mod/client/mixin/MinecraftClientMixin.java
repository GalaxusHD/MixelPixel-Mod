package net.mixelpixel.mod.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.mixelpixel.mod.client.screen.EmptyRadialMenuScreen;
import net.mixelpixel.mod.client.target.PlayerTargeting;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Unique private boolean mixelpixel$pWasDown;

    @Inject(method = "tick", at = @At("TAIL"))
    private void mixelpixel$tick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        PlayerTargeting.tick(client);
        boolean down = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_P);
        if (down && !mixelpixel$pWasDown && client.currentScreen == null && PlayerTargeting.getTarget() != null) {
            client.setScreen(new EmptyRadialMenuScreen(PlayerTargeting.getTarget()));
        }
        mixelpixel$pWasDown = down;
    }
}
