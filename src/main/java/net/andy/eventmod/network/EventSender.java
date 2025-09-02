package net.andy.eventmod.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.andy.eventmod.EventMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class EventSender {
    public static final String SERVER_URL = "http://127.0.0.1:8000";
    private static HttpClient client;

    public static void initialize() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // Clear AI memory on startup
        clearAIMemory();
    }

    public static void sendEvent(JsonObject eventData, PlayerEntity player) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/event"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(eventData.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> handleResponse(response, player))
                    .exceptionally(ex -> {
                        EventMod.LOGGER.error("Failed to send event", ex);
                        return null;
                    });
        } catch (Exception e) {
            EventMod.LOGGER.error("Error building HTTP request", e);
        }
    }

    private static void handleResponse(HttpResponse<String> response, PlayerEntity player) {
        if (response.statusCode() / 100 == 2) {
            try {
                JsonObject resp = JsonParser.parseString(response.body()).getAsJsonObject();
                if (resp.has("reply")) {
                    String reply = resp.get("reply").getAsString();
                    if (!reply.trim().isEmpty() && player != null) {
                        player.sendMessage(Text.of("<Asuka> " + reply), false);
                    }
                }
            } catch (Exception e) {
                EventMod.LOGGER.error("Failed to parse AI response", e);
            }
        }
    }

    private static void clearAIMemory() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/memory/clear"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        if (res.statusCode() / 100 == 2) {
                            EventMod.LOGGER.info("AI memory cleared successfully");
                        }
                    });
        } catch (Exception e) {
            EventMod.LOGGER.error("Failed to clear AI memory", e);
        }
    }
}