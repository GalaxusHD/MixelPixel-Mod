package net.mixelpixel.mod.client.mixin;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;
import net.mixelpixel.mod.client.config.ModConfig;
import net.mixelpixel.mod.client.target.PlayerTargeting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private BufferBuilderStorage bufferBuilders;
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity", at = @At("TAIL"))
    private void mixelpixel$renderTargetOverlay(Entity entity, double x, double y, double z, float tickDelta,
                                                 MatrixStack matrices, VertexConsumerProvider consumers,
                                                 CallbackInfo ci) {
        if (entity != PlayerTargeting.getTarget() || !"overlay".equalsIgnoreCase(ModConfig.get().targetGlowMode)) return;

        int rgb = ModConfig.get().glowRgb();
        OutlineVertexConsumerProvider outline = bufferBuilders.getOutlineVertexConsumers();
        outline.setColor(ColorHelper.getRed(rgb), ColorHelper.getGreen(rgb), ColorHelper.getBlue(rgb), 255);

        matrices.push();
        matrices.scale(1.035F, 1.035F, 1.035F);
        entityRenderDispatcher.render(entity, x / 1.035, y / 1.035, z / 1.035, tickDelta,
                matrices, outline, entityRenderDispatcher.getLight(entity, tickDelta));
        matrices.pop();
    }
}
