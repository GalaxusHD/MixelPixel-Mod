package net.mixelpixel.mod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.mixelpixel.mod.client.NameMcCommand;
import net.mixelpixel.mod.client.target.ServerAccess;

import java.util.List;
import java.util.ArrayList;
import net.mixelpixel.mod.client.config.ModConfig;
import net.mixelpixel.mod.client.target.PlayerTargeting;

public final class EmptyRadialMenuScreen extends Screen {
    private static final int RING_COLOR = 0xB91C211F;
    private static final int SELECTED_COLOR = 0xD9B95757;
    private static final int BORDER_COLOR = 0xD9A0A0A0;
    private static final List<String> KICK_ICON = List.of(
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀",
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⣿⣿⡄",
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⡀⠀⠀⠀⠀⠈⠻⠿⠟⠁",
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⣿⣦⣀⣤⣴⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⠆",
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⠿⠟⢻⣿⣿⣿⣿⠁",
            "⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣿⣿⣿⣿⠇",
            "⣿⣷⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢶⡀⠀⣼⣿⣿⣿⣿⣶⣶⣶⣶⣄",
            "⣿⣿⣿⣿⣦⣄⠀⠀⠀⠀⠀⢰⣦⣤⠄⠀⠀⠙⢿⣿⣿⣟⠛⠛⠛⢿⣿⣷⣄",
            "⣿⣿⣿⣿⣿⣿⣷⡦⠀⠀⠀⠈⠁⢀⣠⣴⣶⡄⠀⠹⣿⣿⡆⠀⠀⠀⠙⢿⣿⡷",
            "⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣶⣾⣿⣿⣿⣿⠏⠀⠀⢸⣿⣿⠁⠀⠀⠀⠀⠀⠉",
            "⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⠁⠀⠀⠀⣿⣿⡏",
            "⠀⠙⠛⠻⣿⣿⣿⣿⣿⣿⡿⠟⠛⠁⠀⠀⠀⠀⠀⠉⠋⠁",
            "⠀⠀⠀⠀⠹⣿⣿⣿⣿⣷",
            "⠀⠀⠀⠀⠀⠘⠻⣿⠟⠁"
    );
    private final List<RadialEntry> entries = enabledEntries();
    private static List<RadialEntry> enabledEntries() {
        ModConfig c = ModConfig.get();
        List<RadialEntry> result = new ArrayList<>();
        if (c.messageEnabled) result.add(new RadialEntry(GuiAssets.RADIAL_MESSAGE, Action.MESSAGE));
        if (c.kickEnabled) result.add(new RadialEntry(GuiAssets.RADIAL_KICK, Action.KICK));
        if (c.banEnabled) result.add(new RadialEntry(GuiAssets.RADIAL_BAN, Action.BAN));
        if (c.reportEnabled) result.add(new RadialEntry(GuiAssets.RADIAL_REPORT, Action.REPORT));
        if (c.nameMcEnabled) result.add(new RadialEntry(GuiAssets.RADIAL_NAMEMC, Action.NAMEMC));
        return List.copyOf(result);
    }
    public static boolean hasActions() { return !enabledEntries().isEmpty(); }
    private final AbstractClientPlayerEntity target;
    private final String targetName;
    private int selected = -1;

    public EmptyRadialMenuScreen(AbstractClientPlayerEntity target) {
        super(Text.literal("Spieler-Menü"));
        this.target = target;
        this.targetName = target.getGameProfile().getName();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x33000000);
        int cx = width / 2;
        int cy = height / 2;
        int outer = Math.max(72, Math.min(112, Math.min(width, height) / 3));
        int inner = Math.round(outer * 0.52F);
        selected = segmentAt(mouseX - cx, mouseY - cy, inner, outer, entries.size());

        drawRing(context, cx, cy, inner, outer + 2, 0xC9000000);
        drawRing(context, cx, cy, inner, outer, RING_COLOR);
        if (selected >= 0) {
            drawSelectedSegment(context, cx, cy, inner, outer, selected, entries.size(), SELECTED_COLOR);
        }
        for (int i = 0; i < (entries.size() == 1 ? 2 : entries.size()); i++) {
            double boundary = entries.size() == 1 ? (i == 0 ? -Math.PI / 4 : Math.PI / 4) : (i - 0.5D) * Math.PI * 2.0D / entries.size();
            int x0 = cx + (int) Math.round(Math.sin(boundary) * inner);
            int y0 = cy - (int) Math.round(Math.cos(boundary) * inner);
            int x1 = cx + (int) Math.round(Math.sin(boundary) * outer);
            int y1 = cy - (int) Math.round(Math.cos(boundary) * outer);
            drawPixelLine(context, x0, y0, x1, y1, BORDER_COLOR);

        }
        for (int i = 0; i < entries.size(); i++) drawIcon(context, entries.get(i).texture(), cx, cy, inner, outer, i, entries.size());
        Text rankColoredName = Text.literal(targetName)
                .styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
        context.drawCenteredTextWithShadow(textRenderer, rankColoredName, cx, cy - 4, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int outer = Math.max(72, Math.min(112, Math.min(width, height) / 3));
        selected = segmentAt((int) Math.round(mouseX - width / 2),
                (int) Math.round(mouseY - height / 2), Math.round(outer * 0.52F), outer, entries.size());
        if (button == 0 && selected >= 0) {
            activate(entries.get(selected).action());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void activate(Action action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ServerAccess.isAllowed(client) || PlayerTargeting.getTarget() != target || !ModConfig.get().targetingEnabled() || target.getWorld() != client.world || !PlayerTargeting.isRealPlayer(client, target) || enabledEntries().stream().noneMatch(entry -> entry.action() == action)) {
            close();
            return;
        }
        switch (action) {
            case KICK -> {
                close();
                client.player.networkHandler.sendChatCommand("p kick " + targetName);
            }
            case BAN -> {
                close();
                client.player.networkHandler.sendChatCommand("p ban " + targetName);
            }
            case MESSAGE -> client.setScreen(new ChatScreen("/msg " + targetName + " "));
            case NAMEMC -> {
                close();
                NameMcCommand.execute(targetName);
            }
            case REPORT -> {
                close();
                client.player.networkHandler.sendChatCommand("report " + targetName);
            }
        }
    }

    private void drawIcon(DrawContext context, Identifier texture, int cx, int cy,
                          int inner, int outer, int index, int count) {
        double angle = RadialLayout.angle(index, count);
        float radius = (inner + outer) * 0.5F;
        float centerX = cx + (float) Math.sin(angle) * radius;
        float centerY = cy - (float) Math.cos(angle) * radius;
        // The regenerated motifs fill ~90% of the texture rather than ~60%.
        // Keep a margin in the ring and scale down on small windows as well.
        int size = Math.max(1, Math.round((outer - inner) * 0.90F));
        if (texture.equals(GuiAssets.RADIAL_NAMEMC)) {
            size = Math.max(1, Math.round(size * 0.80F));
        }
        if (texture.equals(GuiAssets.RADIAL_REPORT)) {
            // Center the asymmetric motif by its opaque-pixel center of mass.
            centerX += size * (63.5F - 55.91F) / 128.0F;
            centerY += size * (63.5F - 74.06F) / 128.0F;
        }
        context.drawTexture(GuiAssets.PIPELINE, texture,
                Math.round(centerX - size / 2.0F), Math.round(centerY - size / 2.0F),
                0, 0, size, size, 128, 128, 128, 128);
    }

    private static int segmentAt(int dx, int dy, int inner, int outer, int count) {
        return RadialLayout.segmentAt(dx, dy, inner, outer, count);
    }

    private static void drawSelectedSegment(DrawContext context, int cx, int cy, int inner, int outer,
                                            int selected, int count, int color) {
        for (int y = -outer; y <= outer; y++) {
            int runStart = Integer.MIN_VALUE;
            for (int x = -outer; x <= outer; x++) {
                boolean inside = segmentAt(x, y, inner, outer, count) == selected;
                if (inside && runStart == Integer.MIN_VALUE) runStart = x;
                if ((!inside || x == outer) && runStart != Integer.MIN_VALUE) {
                    int end = inside && x == outer ? x + 1 : x;
                    context.fill(cx + runStart, cy + y, cx + end, cy + y + 1, color);
                    runStart = Integer.MIN_VALUE;
                }
            }
        }
    }

    private static void drawCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt((long) radius * radius - (long) y * y);
            context.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
        }
    }

    private static void drawRing(DrawContext context, int cx, int cy, int inner, int outer, int color) {
        for (int y = -outer; y <= outer; y++) {
            int outerHalf = (int) Math.sqrt((long) outer * outer - (long) y * y);
            if (Math.abs(y) >= inner) {
                context.fill(cx - outerHalf, cy + y, cx + outerHalf + 1, cy + y + 1, color);
            } else {
                int innerHalf = (int) Math.sqrt((long) inner * inner - (long) y * y);
                context.fill(cx - outerHalf, cy + y, cx - innerHalf, cy + y + 1, color);
                context.fill(cx + innerHalf + 1, cy + y, cx + outerHalf + 1, cy + y + 1, color);
            }
        }
    }

    private record RadialEntry(Identifier texture, Action action) {}

    private enum Action {
        KICK,
        BAN,
        MESSAGE,
        REPORT, NAMEMC
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



