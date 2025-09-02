package net.andy.eventmod.events;

import net.andy.eventmod.EventMod;

public class EventRegistry {
    public static void registerAllEvents() {
        EventMod.LOGGER.info("Registering event handlers...");

        PlayerEventHandlers.register();
        BlockEventHandlers.register();
        CombatEventHandlers.register();
        ItemEventHandlers.register();
//        ServerEventHandlers.register(); TODO: Add join and leave events

        EventMod.LOGGER.info("All event handlers registered!");
    }
}