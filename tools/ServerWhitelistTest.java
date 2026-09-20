import net.mixelpixel.mod.client.target.ServerWhitelist;

public final class ServerWhitelistTest {
    public static void main(String[] args) {
        for (String address : new String[]{"MixelPixel.net", "play.mixelpixel.net", "PLAY.MIXELPIXEL.NET",
                " mixelpixel.net ", "mixelpixel.net:25565", "play.mixelpixel.net:25566"}) {
            if (!ServerWhitelist.allows(address)) throw new AssertionError("Rejected: " + address);
        }
        for (String address : new String[]{null, "", "localhost", "127.0.0.1", "other.net",
                "mixelpixel.net.evil.test", "fake-mixelpixel.net", "other.mixelpixel.net",
                "mixelpixel.net@evil.test", "https://mixelpixel.net", "mixelpixel.net:",
                "mixelpixel.net:0", "mixelpixel.net:65536", "mixelpixel.net:-1",
                "mixelpixel.net:abc", "mixelpixel.net:25565:1", "[::1]:25565"}) {
            if (ServerWhitelist.allows(address)) throw new AssertionError("Allowed: " + address);
        }
        System.out.println("Whitelist: 23 cases passed");
    }
}
