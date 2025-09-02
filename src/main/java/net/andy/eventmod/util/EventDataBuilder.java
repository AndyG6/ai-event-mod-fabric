package net.andy.eventmod.util;

import com.google.gson.JsonObject;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class EventDataBuilder {
    private final JsonObject json;

    private EventDataBuilder(String eventType) {
        this.json = new JsonObject();
        this.json.addProperty("type", eventType);
    }

    public static EventDataBuilder create(String eventType) {
        return new EventDataBuilder(eventType);
    }

    public EventDataBuilder addPlayer(PlayerEntity player) {
        json.addProperty("player", player.getName().getString());
        return this;
    }

    public EventDataBuilder addPosition(BlockPos pos) {
        json.addProperty("pos", pos.toShortString());
        return this;
    }

    public EventDataBuilder addTool(ItemStack stack) {
        if (!stack.isEmpty()) {
            json.addProperty("tool", stack.getItem().toString());
        }
        return this;
    }

    public EventDataBuilder addWeapon(ItemStack stack) {
        if (!stack.isEmpty()) {
            json.addProperty("weapon", stack.getItem().toString());
        }
        return this;
    }

    public EventDataBuilder add(String key, String value) {
        json.addProperty(key, value);
        return this;
    }

    public EventDataBuilder add(String key, boolean value) {
        json.addProperty(key, value);
        return this;
    }

    public EventDataBuilder add(String key, int value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonObject build() {
        return json;
    }
}