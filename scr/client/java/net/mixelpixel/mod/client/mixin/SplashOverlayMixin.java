package net.mixelpixel.mod.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.mixelpixel.mod.client.screen.GuiAssets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Shadow private float progress;

    @Inject(method = "render", at = @At("TAIL"))
    private void mixelpixel$replaceVisuals(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        GuiAssets.drawCover(context, GuiAssets.LOADING, width, height, 2560, 1334);
        int barWidth = Math.min(320, width - 60);
        int x = (width - barWidth) / 2;
        int y = height - Math.max(35, height / 10);
        context.fill(x - 2, y - 2, x + barWidth + 2, y + 8, 0xCC000000);
        context.fill(x, y, x + barWidth, y + 6, 0xFF333333);
        context.fill(x, y, x + Math.round(barWidth * Math.max(0.0F, Math.min(1.0F, progress))), y + 6, 0xFFFFFFFF);
    }
}
