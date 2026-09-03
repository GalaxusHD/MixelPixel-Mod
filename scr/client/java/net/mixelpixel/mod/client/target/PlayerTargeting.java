package net.mixelpixel.mod.client.target;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.mixelpixel.mod.client.config.ModConfig;

public final class PlayerTargeting {
    private static AbstractClientPlayerEntity target;

    private PlayerTargeting() {
    }

    public static AbstractClientPlayerEntity getTarget() {
        return target;
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null) {
            if (!(client.currentScreen instanceof net.mixelpixel.mod.client.screen.EmptyRadialMenuScreen)) target = null;
            return;
        }

        double range = Math.max(1.0, ModConfig.get().targetRange);
        Vec3d start = client.player.getCameraPosVec(1.0F);
        Vec3d end = start.add(client.player.getRotationVec(1.0F).multiply(range));
        Box search = client.player.getBoundingBox().stretch(client.player.getRotationVec(1.0F).multiply(range)).expand(1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                client.player,
                start,
                end,
                search,
                entity -> entity instanceof AbstractClientPlayerEntity && entity != client.player && entity.isAlive() && !entity.isSpectator(),
                range * range
        );

        if (entityHit == null) {
            target = null;
            return;
        }

        if (ModConfig.get().targetMustBeVisible) {
            HitResult blockHit = client.player.raycast(range, 1.0F, false);
            if (blockHit.getType() != HitResult.Type.MISS
                    && blockHit.getPos().squaredDistanceTo(start) < entityHit.getPos().squaredDistanceTo(start)) {
                target = null;
                return;
            }
        }
        target = (AbstractClientPlayerEntity) entityHit.getEntity();
    }
}
