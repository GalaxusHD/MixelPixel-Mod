package net.mixelpixel.mod.client.mixin;
import net.minecraft.resource.*;
import net.mixelpixel.mod.client.resource.ServerPackCache;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

@Mixin(FileResourcePackProvider.class)
public abstract class FileResourcePackProviderMixin {
    @Shadow @Final private ResourceType type;
    @Inject(method = "register", at = @At("TAIL"))
    private void mixelpixel$cachedPack(Consumer<ResourcePackProfile> consumer, CallbackInfo ci) {
        if (type == ResourceType.CLIENT_RESOURCES) ServerPackCache.register(consumer);
    }
}
