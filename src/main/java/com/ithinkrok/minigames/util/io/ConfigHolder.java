package com.ithinkrok.minigames.util.io;

import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.lang.LanguageLookup;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 *
 * Holds things loaded from configs
 */
public interface ConfigHolder {

    void addListener(String name, Listener listener);
    void addCustomItem(CustomItem customItem);
    void addLanguageLookup(LanguageLookup languageLookup);

    //TODO Add ClickableInventories/ClickableItems in future
}
