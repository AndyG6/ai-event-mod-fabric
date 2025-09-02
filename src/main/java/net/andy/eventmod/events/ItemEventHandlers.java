package net.andy.eventmod.events;

import net.andy.eventmod.network.EventSender;
import net.andy.eventmod.util.EventDataBuilder;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public class ItemEventHandlers {
    public static void register() {
        // Use item
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            var eventBuilder = EventDataBuilder.create("use_item")
                    .addPlayer(player)
                    .add("item", stack.getItem().toString())
                    .add("hand", hand.toString());

            // Correct food detection using ConsumableComponent
            ConsumableComponent consumable = stack.get(DataComponentTypes.CONSUMABLE);
            if (consumable != null) {
                eventBuilder.add("is_consumable", true);
                eventBuilder.add("use_action", stack.getUseAction().toString());
                eventBuilder.add("max_use_time", stack.getMaxUseTime(player));
            }


            // Add other item properties as needed
            eventBuilder.add("stack_size", stack.getCount());
            eventBuilder.add("max_stack_size", stack.getMaxCount());

            EventSender.sendEvent(eventBuilder.build(), player);
            return ActionResult.PASS;
        });

        // TODO: Add crafting events (requires mixins)
    }
}