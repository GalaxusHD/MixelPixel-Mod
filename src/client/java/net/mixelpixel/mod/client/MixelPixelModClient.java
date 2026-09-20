package net.mixelpixel.mod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.mixelpixel.mod.client.config.ModConfig;
import org.lwjgl.glfw.GLFW;

public final class MixelPixelModClient implements ClientModInitializer {
    public static final String MOD_ID = "mixelpixelmod";
    public static KeyBinding playerMenuKey;
    public static KeyBinding settingsKey;

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        NameMcCommand.register();
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.resource.ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                    @Override public net.minecraft.util.Identifier getFabricId() {
                        return net.minecraft.util.Identifier.of(MOD_ID, "server_pack_state");
                    }
                    @Override public void reload(net.minecraft.resource.ResourceManager manager) {
                        net.mixelpixel.mod.client.resource.ServerPackCache.reloaded(manager);
                    }
                });
        // GLFW names physical keys by the US layout; this is # on a German keyboard.
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mixelpixelmod.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH,
                "category.mixelpixelmod"
        ));
        playerMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mixelpixelmod.player_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.mixelpixelmod"
        ));
    }

    public static Text versionText() {
        return Text.translatable("mixelpixelmod.version");
    }
}


