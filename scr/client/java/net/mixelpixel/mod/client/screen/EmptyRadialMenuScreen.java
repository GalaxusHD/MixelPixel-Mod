package net.mixelpixel.mod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;

public final class EmptyRadialMenuScreen extends Screen {
    private final AbstractClientPlayerEntity target;

    public EmptyRadialMenuScreen(AbstractClientPlayerEntity target) {
        super(Text.literal("Spieler-Menü"));
        this.target = target;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x66000000);
        int cx = width / 2;
        int cy = height / 2;
        int outer = Math.min(width, height) / 4;
        drawCircle(context, cx, cy, outer + 2, 0xFF0A0A0A);
        drawCircle(context, cx, cy, outer, 0xCC2B2B2B);
        drawCircle(context, cx, cy, Math.max(34, outer / 2), 0xEE151515);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            int x = cx + (int) (Math.cos(angle) * outer);
            int y = cy + (int) (Math.sin(angle) * outer);
            drawPixelLine(context, cx, cy, x, y, 0xFF777777);
        }
        context.drawCenteredTextWithShadow(textRenderer, target.getDisplayName(), cx, cy - 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("mixelpixelmod.radial.empty"), cx, cy + 8, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    private static void drawCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt((long) radius * radius - (long) y * y);
            context.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
        }
    }

    private static void drawPixelLine(DrawContext context, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        while (true) {
            context.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int twice = error * 2;
            if (twice >= dy) { error += dy; x0 += sx; }
            if (twice <= dx) { error += dx; y0 += sy; }
        }
    }
}
