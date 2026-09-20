package net.mixelpixel.mod.client.resource;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

/** Immutable, content-addressed packs survive disconnects; only actual changes trigger a reload. */
public final class ServerPackCache {
    private static final String PREFIX = "mixelpixelmod:server/";
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("mixelpixel-server-pack");
    private static String currentHash;
    private static volatile String loadedHash = "";
    private static CompletableFuture<Void> queue = CompletableFuture.completedFuture(null);
    private static final Map<ClientConnection, UUID> primaryPacks = new WeakHashMap<>();
    private static final ExecutorService DOWNLOADS = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MixelPixel-pack-cache"); t.setDaemon(true); return t;
    });
    private ServerPackCache() {}
    private static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes));
    }
    private static Path pack(String hash) { return DIR.resolve(hash + ".zip"); }
    private static void validate(Path path) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            if (zip.getEntry("pack.mcmeta") == null) throw new IOException("pack.mcmeta fehlt");
        }
    }
    private static synchronized void ensureInitial() throws Exception {
        if (currentHash != null) return;
        Files.createDirectories(DIR);
        Path pointer = DIR.resolve("current.txt");
        if (Files.exists(pointer)) {
            String saved = Files.readString(pointer).trim();
            if (saved.matches("[0-9a-f]{40}") && Files.exists(pack(saved))) {
                try {
                    validate(pack(saved));
                    if (hash(Files.readAllBytes(pack(saved))).equals(saved)) { currentHash = saved; return; }
                } catch (IOException invalidCache) { /* Fall back to bundled known-good pack. */ }
            }
        }
        try (InputStream in = ServerPackCache.class.getResourceAsStream("/mixelpixel/server-pack.zip")) {
            if (in == null) throw new IOException("Eingebautes Serverpaket fehlt");
            byte[] bytes = in.readAllBytes(); String initial = hash(bytes);
            Files.write(pack(initial), bytes); validate(pack(initial)); currentHash = initial;
        }
    }
    public static void register(Consumer<ResourcePackProfile> consumer) {
        try {
            ensureInitial();
            ResourcePackProfile profile = ResourcePackProfile.create(
                new ResourcePackInfo(PREFIX + currentHash, Text.literal("MixelPixel Serverressourcen"), ResourcePackSource.BUILTIN, Optional.empty()),
                new ZipResourcePack.ZipBackedFactory(pack(currentHash)), ResourceType.CLIENT_RESOURCES,
                new ResourcePackPosition(true, ResourcePackProfile.InsertionPosition.TOP, true));
            if (profile != null) consumer.accept(profile);
        } catch (Exception e) { LoggerFactory.getLogger("MixelPixel").error("Serverpaket konnte nicht geladen werden", e); }
    }
    public static void reloaded(ResourceManager manager) {
        loadedHash = manager.streamResourcePacks().map(ResourcePack::getId)
            .filter(id -> id.startsWith(PREFIX)).map(id -> id.substring(PREFIX.length())).findFirst().orElse("");
    }
    private static void status(ClientConnection connection, UUID id, Status status) {
        if (connection.isOpen()) connection.send(new ResourcePackStatusC2SPacket(id, status));
    }
    public static synchronized boolean handle(MinecraftClient client, ClientConnection connection, ResourcePackSendS2CPacket packet) {
        UUID primary = primaryPacks.computeIfAbsent(connection, ignored -> packet.id());
        // Additional event packs still use Minecraft's own stack.
        if (!primary.equals(packet.id())) return false;
        status(connection, packet.id(), Status.ACCEPTED);
        queue = queue.handle((v, e) -> null).thenCompose(v -> {
            if (!connection.isOpen()) return CompletableFuture.completedFuture(null);
            if (!loadedHash.isEmpty() && loadedHash.equalsIgnoreCase(packet.hash())) {
                status(connection, packet.id(), Status.DOWNLOADED);
                status(connection, packet.id(), Status.SUCCESSFULLY_LOADED);
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.supplyAsync(() -> download(packet), DOWNLOADS)
                .thenCompose(downloaded -> client.submit(() -> apply(client, connection, packet.id(), downloaded)).thenCompose(f -> f))
                .exceptionally(e -> {
                    LoggerFactory.getLogger("MixelPixel").warn("Serverpaket-Update fehlgeschlagen; bisheriger Stand bleibt erhalten", e);
                    status(connection, packet.id(), Status.FAILED_DOWNLOAD); return null;
                });
        });
        return true;
    }
    private static String download(ResourcePackSendS2CPacket packet) {
        try {
            ensureInitial();
            URI uri = URI.create(packet.url());
            if (!List.of("https", "http").contains(uri.getScheme())) throw new IOException("Ungültige Paket-URL");
            try (HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15))
                    .followRedirects(HttpClient.Redirect.NORMAL).build()) {
                HttpResponse<InputStream> response = http.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(60)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream in = response.body()) {
                    if (response.statusCode() != 200) throw new IOException("HTTP " + response.statusCode());
                    byte[] bytes = in.readNBytes(64 * 1024 * 1024 + 1);
                    if (bytes.length > 64 * 1024 * 1024) throw new IOException("Serverpaket größer als 64 MiB");
                    String digest = hash(bytes);
                    if (packet.hash().matches("(?i)[0-9a-f]{40}") && !digest.equalsIgnoreCase(packet.hash()))
                        throw new IOException("Serverpaket-Prüfsumme stimmt nicht überein");
                    if (Files.exists(pack(digest)) && hash(Files.readAllBytes(pack(digest))).equals(digest)) return digest;
                    Path temp = Files.createTempFile(DIR, "download-", ".zip");
                    try {
                        Files.write(temp, bytes); validate(temp);
                        Files.move(temp, pack(digest), StandardCopyOption.REPLACE_EXISTING);
                    } finally { Files.deleteIfExists(temp); }
                    return digest;
                }
            }
        } catch (Exception e) { throw new CompletionException(e); }
    }
    private static CompletableFuture<Void> apply(MinecraftClient client, ClientConnection connection, UUID id, String digest) {
        if (!connection.isOpen()) return CompletableFuture.completedFuture(null);
        status(connection, id, Status.DOWNLOADED);
        if (digest.equals(loadedHash)) {
            status(connection, id, Status.SUCCESSFULLY_LOADED); return CompletableFuture.completedFuture(null);
        }
        String previous = currentHash;
        currentHash = digest;
        client.getResourcePackManager().scanPacks();
        return client.reloadResources().handle((v, failure) -> {
            if (failure == null && digest.equals(loadedHash)) {
                try { Files.writeString(DIR.resolve("current.txt"), digest); }
                catch (IOException e) { LoggerFactory.getLogger("MixelPixel").warn("Paketstand konnte nicht gespeichert werden", e); }
                status(connection, id, Status.SUCCESSFULLY_LOADED);
            } else {
                currentHash = previous;
                client.getResourcePackManager().scanPacks();
                status(connection, id, Status.FAILED_RELOAD);
                client.reloadResources();
            }
            return null;
        });
    }
}


