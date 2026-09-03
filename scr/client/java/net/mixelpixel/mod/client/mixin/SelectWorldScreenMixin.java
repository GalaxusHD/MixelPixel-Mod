package net.mixelpixel.mod.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.mixelpixel.mod.client.screen.GuiAssets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void mixelpixel$background(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GuiAssets.drawCover(context, GuiAssets.WORLDS,
                context.getScaledWindowWidth(), context.getScaledWindowHeight(), 2560, 1334);
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x33000000);
    }
}
