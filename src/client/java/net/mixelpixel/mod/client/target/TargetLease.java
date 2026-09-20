package net.mixelpixel.mod.client.target;

import java.util.Objects;
import java.util.UUID;

/** A selection expires even when the crosshair remains on the same player. */
public final class TargetLease {
    private UUID selected;
    private UUID lookedAt;
    private long selectedAt;
    public static int clampSeconds(int seconds) { return Math.max(3, Math.min(120, seconds)); }
    public UUID current(long nowNanos, int seconds) {
        if (selected != null && nowNanos - selectedAt >= clampSeconds(seconds) * 1_000_000_000L) selected = null;
        return selected;
    }
    public void lookAt(UUID candidate, long nowNanos) {
        if (candidate != null && !Objects.equals(candidate, lookedAt)) {
            selected = candidate;
            selectedAt = nowNanos;
        }
        lookedAt = candidate;
    }
    public void clear() { selected = null; lookedAt = null; }
}
