package net.mixelpixel.mod.client.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import net.mixelpixel.mod.client.MixelPixelModClient;

public final class GuiAssets {
    public static final Identifier TITLE = id("textures/gui/title.png");
    public static final Identifier WORLDS = id("textures/gui/worlds.png");
    public static final Identifier MULTIPLAYER = id("textures/gui/multiplayer.png");
    public static final Identifier LOADING = id("textures/gui/loading.png");
    public static final Identifier LOGO = id("textures/gui/logo-mod-v1.png");
    public static final Identifier SERVER_HOVER = id("textures/gui/server_hover.png");
    public static final Identifier RADIAL_KICK = id("textures/gui/radial/kick.png");
    public static final Identifier RADIAL_BAN = id("textures/gui/radial/ban.png");
    public static final Identifier RADIAL_MESSAGE = id("textures/gui/radial/message.png");
    public static final Identifier RADIAL_NAMEMC = id("textures/gui/radial/menu-symbol.png");
    public static final Identifier RADIAL_REPORT = id("textures/gui/radial/report.png");
    public static final RenderPipeline PIPELINE = RenderPipelines.GUI_TEXTURED;

    private GuiAssets() {
    }

    private static Identifier id(String path) {
        return Identifier.of(MixelPixelModClient.MOD_ID, path);
    }

    public static void drawCover(DrawContext context, Identifier texture, int width, int height, int textureWidth, int textureHeight) {
        drawCover(context, texture, width, height, textureWidth, textureHeight, 0xFFFFFFFF);
    }

    public static void drawCover(DrawContext context, Identifier texture, int width, int height, int textureWidth, int textureHeight, int color) {
        double screenRatio = (double) width / height;
        double textureRatio = (double) textureWidth / textureHeight;
        float u = 0;
        float v = 0;
        float visibleWidth = textureWidth;
        float visibleHeight = textureHeight;
        if (screenRatio > textureRatio) {
            visibleHeight = (float) (textureWidth / screenRatio);
            v = (textureHeight - visibleHeight) / 2.0F;
        } else {
            visibleWidth = (float) (textureHeight * screenRatio);
            u = (textureWidth - visibleWidth) / 2.0F;
        }
        context.drawTexture(PIPELINE, texture, 0, 0, u, v, width, height, Math.round(visibleWidth), Math.round(visibleHeight), textureWidth, textureHeight, color);
    }
}

