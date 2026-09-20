package net.mixelpixel.mod.client.target;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public final class ServerAccess {
    private ServerAccess() {}
    public static boolean isAllowed(MinecraftClient client) {
        if (client == null || client.isInSingleplayer() || client.world == null
                || client.player == null || client.getNetworkHandler() == null) return false;
        ServerInfo server = client.getCurrentServerEntry();
        return server != null && !server.isLocal() && ServerWhitelist.allows(server.address);
    }
}
