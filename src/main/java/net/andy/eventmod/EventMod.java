package net.andy.eventmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse; // for BodyHandlers
import java.time.Duration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;



public class EventMod implements ModInitializer {
    public static final String MOD_ID = "event-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Reuse ONE client (good practice)
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] initialized", MOD_ID);

        // at the top of onInitialize(), before registering callbacks
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8000/memory/clear"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString("{}")) // empty JSON body
                    .build();

            CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        if (res.statusCode() / 100 == 2) {
                            LOGGER.info("[{}] Cleared AI memory on server init", MOD_ID);
                        } else {
                            LOGGER.warn("[{}] Clear memory failed (status {})", MOD_ID, res.statusCode());
                        }
                    })
                    .exceptionally(ex -> { LOGGER.error("HTTP error clearing memory", ex); return null; });
        } catch (Exception e) {
            LOGGER.error("Failed to send clear request", e);
        }


        // Example: attack entity
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Only run on the logical server
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            JsonObject json = new JsonObject();
            json.addProperty("type", "attack_entity");
            json.addProperty("player", player.getName().getString());
            json.addProperty("entity", entity.getType().toString());
            sendEvent(json, player);
            return ActionResult.PASS;
        });

        // Example: block break
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            // Only run on the logical server
            if (world.isClient()) {
                return;
            }
            JsonObject json = new JsonObject();
            json.addProperty("type", "block_break");
            json.addProperty("player", player.getName().getString());
            json.addProperty("block", state.getBlock().toString()); // or s
            json.addProperty("pos", pos.toShortString());
            sendEvent(json, player);
        });

        // New: player text/chat event
        ServerMessageEvents.CHAT_MESSAGE.register((SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) -> {
            // Only run on the server (this event is server-side only)
            String playerName = sender.getName().getString();
            String text = message.getContent().getString();

            JsonObject json = new JsonObject();
            json.addProperty("type", "player_chat");
            json.addProperty("player", playerName);
            json.addProperty("text", text);

            sendEvent(json, sender);
        });
    }
    // version 2 — with player chat feedback
    private void sendEvent(JsonObject json, PlayerEntity player) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8000/event"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        if (res.statusCode() / 100 == 2) {
                            JsonObject resp = JsonParser.parseString(res.body()).getAsJsonObject();
                            if (resp.has("reply")) {
                                String reply = resp.get("reply").getAsString();
                                if (player != null) {
                                    player.sendMessage(Text.of("<Asuka> " + reply), false);
                                } else {
                                    LOGGER.info("<Asuka>> {}", reply);
                                }
                            }
                        }
                    })
                    .exceptionally(ex -> { LOGGER.error("HTTP error", ex); return null; });

        } catch (Exception e) {
            LOGGER.error("Failed to build/send request", e);
        }
    }
}
