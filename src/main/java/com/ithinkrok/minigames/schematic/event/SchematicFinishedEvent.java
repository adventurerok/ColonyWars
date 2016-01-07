package com.ithinkrok.minigames.schematic.event;

import com.ithinkrok.minigames.schematic.PastedSchematic;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicFinishedEvent extends Event {

    private final PastedSchematic schematic;

    public SchematicFinishedEvent(PastedSchematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
