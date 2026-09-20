package net.mixelpixel.mod.client.screen;

/** Angles run clockwise from twelve o'clock; priorities run top, left, right, bottom. */
public final class RadialLayout {
    private RadialLayout() {}
    public static int slot(int priority, int count) {
        return switch (count) {
            case 3 -> new int[]{0, 2, 1}[priority];
            case 5 -> new int[]{0, 4, 1, 3, 2}[priority];
            case 4 -> new int[]{0, 3, 1, 2}[priority];
            default -> priority;
        };
    }
    public static double angle(int priority, int count) { return slot(priority, count) * Math.PI * 2 / count; }
    public static int segmentAt(double dx, double dy, int inner, int outer, int count) {
        double distance = dx * dx + dy * dy;
        if (count < 1 || distance < inner * inner || distance > outer * outer) return -1;
        double angle = Math.atan2(dx, -dy);
        if (count == 1) return Math.abs(angle) <= Math.PI / 4 ? 0 : -1;
        if (angle < 0) angle += Math.PI * 2;
        double sector = Math.PI * 2 / count;
        int slot = (int) Math.floor((angle + sector / 2) / sector) % count;
        for (int i = 0; i < count; i++) if (slot(i, count) == slot) return i;
        return -1;
    }
}
