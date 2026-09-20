package net.mixelpixel.mod.client.mixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.*;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.mixelpixel.mod.client.resource.ServerPackCache;
import net.mixelpixel.mod.client.target.ServerWhitelist;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {
    @Shadow @Final protected MinecraftClient client;
    @Shadow @Final protected ClientConnection connection;
    @Shadow @Final protected ServerInfo serverInfo;
    @Inject(method = "onResourcePackSend", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void mixelpixel$checkPack(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        if (serverInfo != null && !serverInfo.isLocal() && ServerWhitelist.allows(serverInfo.address)
                && ServerPackCache.handle(client, connection, packet)) ci.cancel();
    }
}
