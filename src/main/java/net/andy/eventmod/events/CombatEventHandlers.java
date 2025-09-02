package net.andy.eventmod.events;

import net.andy.eventmod.network.EventSender;
import net.andy.eventmod.util.EventDataBuilder;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;

public class CombatEventHandlers {
    public static void register() {
        // Attack entity
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            var eventData = EventDataBuilder.create("attack_entity")
                    .addPlayer(player)
                    .add("entity", entity.getType().toString())
                    .add("hand", hand.toString())
                    .addWeapon(player.getStackInHand(hand))
                    .build();
            EventSender.sendEvent(eventData, player);
            return ActionResult.PASS;
        });

        // TODO: Add damage events (requires mixins)
    }
}