package net.mixelpixel.mod.client.target;

import java.util.Locale;
import java.util.Set;

/** Exact connection-host whitelist, not server display names or substrings. */
public final class ServerWhitelist {
    private static final Set<String> HOSTS = Set.of("mixelpixel.net", "play.mixelpixel.net");
    private ServerWhitelist() {}

    public static boolean allows(String address) {
        if (address == null) return false;
        String host = address.strip().toLowerCase(Locale.ROOT);
        int colon = host.indexOf(':');
        if (colon >= 0) {
            String port = host.substring(colon + 1);
            if (!port.matches("[0-9]{1,5}")) return false;
            int value = Integer.parseInt(port);
            if (value < 1 || value > 65535) return false;
            host = host.substring(0, colon);
        }
        return HOSTS.contains(host);
    }
}
