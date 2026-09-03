package net.mixelpixel.mod.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.mixelpixel.mod.client.screen.GuiAssets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class LoadingScreenBackgroundMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void mixelpixel$loadingBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Object screen = this;
        boolean loading = screen instanceof ConnectScreen
                || screen instanceof LevelLoadingScreen
                || screen instanceof DownloadingTerrainScreen
                || screen instanceof ProgressScreen
                || screen.getClass().getSimpleName().contains("RealmsConnecting");
        if (!loading) return;
        GuiAssets.drawCover(context, GuiAssets.LOADING,
                context.getScaledWindowWidth(), context.getScaledWindowHeight(), 2560, 1334);
        ci.cancel();
    }
}
