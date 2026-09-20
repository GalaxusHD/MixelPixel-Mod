package net.mixelpixel.mod.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.util.Identifier;
import net.mixelpixel.mod.client.screen.GuiAssets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    // Logo halves are rendered even during the final fade without a progress bar.
    // Replace only these draws, preserving vanilla reload/error/completion handling.
    @Redirect(method = "render", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V"),
            require = 2, allow = 2)
    private void mixelpixel$replaceLogo(DrawContext context, RenderPipeline pipeline, Identifier texture,
                                      int x, int y, float u, float v, int width, int height,
                                      int regionWidth, int regionHeight, int textureWidth, int textureHeight,
                                      int color) {
        // Left half has negative U: draw once and suppress both vanilla halves.
        if (u < 0.0F) {
            context.createNewRootLayer();
            GuiAssets.drawCover(context, GuiAssets.LOADING,
                    context.getScaledWindowWidth(), context.getScaledWindowHeight(), 2560, 1334, color);
        }
    }
}

