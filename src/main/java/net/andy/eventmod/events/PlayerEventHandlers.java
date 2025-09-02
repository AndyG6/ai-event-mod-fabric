package net.andy.eventmod.events;

import net.andy.eventmod.network.EventSender;
import net.andy.eventmod.util.EventDataBuilder;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class PlayerEventHandlers {
    public static void register() {
        // Player chat
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            var eventData = EventDataBuilder.create("player_chat")
                    .addPlayer(sender)
                    .add("text", message.getContent().getString())
                    .build();
            EventSender.sendEvent(eventData, sender);
        });

        // Player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            var eventData = EventDataBuilder.create("player_join")
                    .addPlayer(player)
                    .addPosition(player.getBlockPos())
                    .add("dimension", player.getWorld().getRegistryKey().getValue().toString())
                    .build();
            EventSender.sendEvent(eventData, player);
        });

        // Player leave
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            var eventData = EventDataBuilder.create("player_leave")
                    .addPlayer(player)
                    .build();
            EventSender.sendEvent(eventData, null);
        });

        // Player respawn
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            var eventData = EventDataBuilder.create("player_respawn")
                    .addPlayer(newPlayer)
                    .add("was_alive", alive)
                    .addPosition(newPlayer.getBlockPos())
                    .build();
            EventSender.sendEvent(eventData, newPlayer);
        });
    }
}