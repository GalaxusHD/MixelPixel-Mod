package net.mixelpixel.mod.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.mixelpixel.mod.client.MixelPixelModClient;
import net.mixelpixel.mod.client.screen.ActionWidget;
import net.mixelpixel.mod.client.screen.GuiAssets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private static final Identifier MINECRAFT_LOGO = Identifier.ofVanilla("textures/gui/title/minecraft.png");
    private static final Identifier EDITION_LOGO = Identifier.ofVanilla("textures/gui/title/edition.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void mixelpixel$init(CallbackInfo ci) {
        ci.cancel();
        clearChildren();
        int center = width / 2;
        int buttonWidth = Math.min(400, Math.max(200, width - 40));
        int x = center - buttonWidth / 2;
        int top = Math.max(102, height / 2 - 30);

        addDrawableChild(new ActionWidget(x, top, buttonWidth, 20, Text.translatable("menu.singleplayer"),
                () -> client.setScreen(new SelectWorldScreen(this)), null, 0, 0, false));
        addDrawableChild(new ActionWidget(x, top + 24, buttonWidth, 20, Text.translatable("menu.multiplayer"),
                () -> client.setScreen(new MultiplayerScreen(this)), null, 0, 0, false));
        addDrawableChild(new ActionWidget(x, top + 48, buttonWidth, 20, Text.translatable("mixelpixelmod.server"),
                this::mixelpixel$connect, GuiAssets.SERVER_HOVER, 2560, 372, false));

        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            addDrawableChild(new ActionWidget(x, top + 72, buttonWidth, 20, Text.literal("Mods"),
                    this::mixelpixel$openModMenu, null, 0, 0, false));
        }

        addDrawableChild(new ActionWidget(center - 200, top + 108, 196, 20, Text.translatable("menu.options"),
                () -> client.setScreen(new OptionsScreen(this, client.options)), null, 0, 0, false));
        addDrawableChild(new ActionWidget(center + 4, top + 108, 196, 20, Text.translatable("menu.quit"),
                client::scheduleStop, null, 0, 0, false));

        int logoSize = Math.max(50, Math.min(90, height / 5));
        addDrawableChild(new ActionWidget(16, height - logoSize - 36, logoSize, logoSize, Text.empty(),
                this::mixelpixel$openVotes, null, 0, 0, true));
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void mixelpixel$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        GuiAssets.drawCover(context, GuiAssets.TITLE, width, height, 2560, 1334);

        int logoWidth = Math.min(420, width - 80);
        int logoHeight = logoWidth * 44 / 256;
        int logoX = (width - logoWidth) / 2;
        int logoY = Math.max(18, height / 14);
        context.drawTexture(GuiAssets.PIPELINE, MINECRAFT_LOGO, logoX, logoY, 0, 0, logoWidth, logoHeight, 256, 44);
        int editionWidth = logoWidth / 2;
        context.drawTexture(GuiAssets.PIPELINE, EDITION_LOGO, (width - editionWidth) / 2,
                logoY + logoHeight - 3, 0, 0, editionWidth, editionWidth * 14 / 128, 128, 14);

        float pulse = 1.0F + 0.08F * (float) Math.sin(Util.getMeasuringTimeMs() / 180.0);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(width / 2.0F + logoWidth * 0.36F, logoY + logoHeight * 0.75F);
        context.getMatrices().rotate((float) Math.toRadians(-20));
        context.getMatrices().scale(pulse, pulse);
        context.drawCenteredTextWithShadow(textRenderer, "MixelPixel auf die 1!", 0, 0, 0xFFFF00);
        context.getMatrices().popMatrix();

        int smallLogo = Math.max(50, Math.min(90, height / 5));
        int smallX = 16;
        int smallY = height - smallLogo - 36;
        context.drawTexture(GuiAssets.PIPELINE, GuiAssets.LOGO, smallX, smallY, 0, 0,
                smallLogo, smallLogo, 512, 512);
        context.drawTextWithShadow(textRenderer, MixelPixelModClient.versionText(), 16, height - 22, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private void mixelpixel$connect() {
        String address = "MixelPixel.net";
        ServerInfo info = new ServerInfo("MixelPixel", address, ServerInfo.ServerType.OTHER);
        ConnectScreen.connect(this, client, ServerAddress.parse(address), info, false, new CookieStorage(Map.of()));
    }

    private void mixelpixel$openVotes() {
        String username = URLEncoder.encode(MinecraftClient.getInstance().getSession().getUsername(), StandardCharsets.UTF_8);
        Util.getOperatingSystem().open(URI.create("https://minecraft-server.eu/vote/index/208F7/" + username));
        Util.getOperatingSystem().open(URI.create("https://www.minecraft-serverlist.net/vote/51076/" + username));
    }

    private void mixelpixel$openModMenu() {
        try {
            Class<?> screenClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            client.setScreen((Screen) screenClass.getConstructor(Screen.class).newInstance(this));
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
