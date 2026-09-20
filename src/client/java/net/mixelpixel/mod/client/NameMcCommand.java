package net.mixelpixel.mod.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/** NameMC profile lookup integrated from GalaxusHD's standalone mod. */
public final class NameMcCommand {
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private NameMcCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
            dispatcher.register(ClientCommandManager.literal("namemc")
                .then(ClientCommandManager.argument("username", StringArgumentType.word())
                    .suggests((context, builder) -> CommandSource.suggestMatching(
                        context.getSource().getPlayerNames(), builder))
                    .executes(context -> show(context.getSource(),
                        StringArgumentType.getString(context, "username"))))));
    }

    public static void execute(String username) {
        MinecraftClient client = MinecraftClient.getInstance();
        var dispatcher = ClientCommandManager.getActiveDispatcher();
        if (dispatcher == null || client.getNetworkHandler() == null) return;
        try {
            dispatcher.execute("namemc " + username,
                (FabricClientCommandSource) client.getNetworkHandler().getCommandSource());
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException error) {
            if (client.player != null) client.player.sendMessage(Text.literal(error.getMessage()), false);
        }
    }

    private static int show(FabricClientCommandSource source, String username) {
        if (!username.matches("[A-Za-z0-9_]{1,16}")) {
            source.sendError(Text.literal("Ungültiger Minecraft-Spielername."));
            return 0;
        }
        source.sendFeedback(Text.literal("NameMC-Profil von " + username + " öffnen")
            .formatted(Formatting.AQUA, Formatting.UNDERLINE)
            .styled(style -> style.withClickEvent(new ClickEvent.OpenUrl(
                URI.create("https://namemc.com/profile/" + username)))));
        source.sendFeedback(Text.literal("Frühere Namen werden geladen …"));
        var connection = source.getClient().getNetworkHandler();
        var request = HttpRequest.newBuilder(URI.create(
            "https://liforra.de/api/namehistory?username=" + username))
            .timeout(Duration.ofSeconds(15)).header("Accept", "application/json").GET().build();
        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() != 200 || response.body().length() > 1_000_000)
                throw new IllegalStateException("Name history unavailable");
            var names = new LinkedHashSet<String>();
            collectNames(JsonParser.parseString(response.body()), names);
            names.removeIf(name -> name.equalsIgnoreCase(username));
            return names.isEmpty() ? "Keine früheren Namen gefunden."
                : "Frühere Namen: " + String.join(", ", names);
        }).exceptionally(error -> "Namensverlauf derzeit nicht abrufbar. Der NameMC-Link bleibt verfügbar.")
            .thenAccept(message -> source.getClient().execute(() -> {
                if (source.getClient().getNetworkHandler() == connection)
                    source.sendFeedback(Text.literal(message));
            }));
        return 1;
    }

    private static void collectNames(JsonElement value, LinkedHashSet<String> names) {
        if (value.isJsonArray()) value.getAsJsonArray().forEach(item -> collectNames(item, names));
        else if (value.isJsonObject()) value.getAsJsonObject().entrySet().forEach(entry -> {
            if (entry.getKey().equals("name") && entry.getValue().isJsonPrimitive()) {
                String name = entry.getValue().getAsString();
                if (name.matches("[A-Za-z0-9_]{1,16}")) names.add(name);
            } else if (entry.getValue().isJsonArray() || entry.getValue().isJsonObject())
                collectNames(entry.getValue(), names);
        });
    }
}
