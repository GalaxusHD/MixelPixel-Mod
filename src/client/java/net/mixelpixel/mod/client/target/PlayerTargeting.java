package net.mixelpixel.mod.client.target;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.mixelpixel.mod.client.config.ModConfig;

public final class PlayerTargeting {
    private static AbstractClientPlayerEntity target;
    private static final TargetLease lease = new TargetLease();

    private PlayerTargeting() {
    }

    public static AbstractClientPlayerEntity getTarget() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (target != null && isInvisible(target)) {
            target = null;
            lease.clear();
        }
        if (!ServerAccess.isAllowed(client) || !ModConfig.get().targetingEnabled() || target == null || target.getWorld() != client.world || !isRealPlayer(client, target)
                || !target.getUuid().equals(lease.current(System.nanoTime(), ModConfig.get().selectionSeconds()))) return null;
        return target;
    }

    private static boolean isInvisible(AbstractClientPlayerEntity player) {
        // Exclude invisibility even when a team rule would let the local player see it.
        return player.isInvisible() || player.hasStatusEffect(StatusEffects.INVISIBILITY);
    }

    public static boolean isRealPlayer(MinecraftClient client, AbstractClientPlayerEntity player) {
        if (client.getNetworkHandler() == null || player == null) return false;
        // NPC player entities often have skin profiles but are not real listed players.
        return client.getNetworkHandler().getListedPlayerListEntries().stream().anyMatch(entry ->
                entry.getProfile().getId().equals(player.getUuid())
                && entry.getProfile().getName().equals(player.getGameProfile().getName())
                && entry.getProfile().getName().matches("[A-Za-z0-9_]{1,16}"));
    }

    public static AbstractClientPlayerEntity getMarkedTarget() {
        return ModConfig.get().markerVisible() ? getTarget() : null;
    }
    public static void tick(MinecraftClient client) {
        if (!ServerAccess.isAllowed(client) || !ModConfig.get().targetingEnabled()) {
            target = null;
            lease.clear();
            if (client.currentScreen instanceof net.mixelpixel.mod.client.screen.EmptyRadialMenuScreen) {
                client.setScreen(null);
            }
            return;
        }
        if (lease.current(System.nanoTime(), ModConfig.get().selectionSeconds()) == null) {
            target = null;
            if (client.currentScreen instanceof net.mixelpixel.mod.client.screen.EmptyRadialMenuScreen) client.setScreen(null);
        }
        if (target != null) {
            var replacement = client.world.getPlayerByUuid(target.getUuid());
            if (replacement instanceof AbstractClientPlayerEntity player) target = player;
            if (isInvisible(target)) {
                target = null;
                lease.clear();
                if (client.currentScreen instanceof net.mixelpixel.mod.client.screen.EmptyRadialMenuScreen)
                    client.setScreen(null);
            }
        }
        if (client.player == null || client.world == null || client.currentScreen != null) {

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
                entity -> entity instanceof AbstractClientPlayerEntity player && !isInvisible(player) && isRealPlayer(client, player) && entity != client.player && entity.isAlive() && !entity.isSpectator(),
                range * range
        );

        if (entityHit == null) {
            lease.lookAt(null, System.nanoTime());
            return;
        }

        if (ModConfig.get().targetMustBeVisible) {
            HitResult blockHit = client.player.raycast(range, 1.0F, false);
            if (blockHit.getType() != HitResult.Type.MISS
                    && blockHit.getPos().squaredDistanceTo(start) < entityHit.getPos().squaredDistanceTo(start)) {
                lease.lookAt(null, System.nanoTime());
                return;
            }
        }
        AbstractClientPlayerEntity candidate = (AbstractClientPlayerEntity) entityHit.getEntity();
        lease.lookAt(candidate.getUuid(), System.nanoTime());
        target = candidate.getUuid().equals(lease.current(System.nanoTime(), ModConfig.get().selectionSeconds())) ? candidate : null;
    }
}


