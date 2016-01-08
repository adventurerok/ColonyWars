package com.ithinkrok.minigames.schematic.event;

import com.ithinkrok.minigames.schematic.PastedSchematic;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicEvent extends Event {

    private final PastedSchematic schematic;

    public SchematicEvent(PastedSchematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }

    public PastedSchematic getSchematic() {
        return schematic;
    }
}
