package net.andy.eventmod.events;

import net.andy.eventmod.network.EventSender;
import net.andy.eventmod.util.EventDataBuilder;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;

public class BlockEventHandlers {
    public static void register() {
        // Block break
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return;

            var eventData = EventDataBuilder.create("block_break")
                    .addPlayer(player)
                    .add("block", state.getBlock().toString())
                    .addPosition(pos)
                    .addTool(player.getMainHandStack())
                    .build();
            EventSender.sendEvent(eventData, player);
        });

        // Block place/interact
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            var stack = player.getStackInHand(hand);
            if (!stack.isEmpty()) {
                var eventData = EventDataBuilder.create("use_block")
                        .addPlayer(player)
                        .add("item", stack.getItem().toString())
                        .addPosition(hitResult.getBlockPos())
                        .add("hand", hand.toString())
                        .build();
                EventSender.sendEvent(eventData, player);
            }
            return ActionResult.PASS;
        });
    }
}