package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.item.event.CustomItemLoreCalculateEvent;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

/**
 * Created by paul on 21/01/16.
 */
public class ExplosionWand implements CustomListener {

    private int maxRange;
    private Calculator explosionPower;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        maxRange = config.getInt("max_range", 100);
        explosionPower = new ExpressionCalculator(config.getString("explosion_power"));
    }

    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        if (event.getBlockFace() == null) return;

        Block target = event.getUser().rayTraceBlocks(maxRange);
        if (target == null) return;

        BlockFace mod = event.getBlockFace();
        Location loc = target.getLocation().clone();
        loc.add(mod.getModX() + 0.5, mod.getModY() + 0.5, mod.getModZ() + 0.5);

        event.getUser()
                .createExplosion(loc, (float) explosionPower.calculate(event.getUser().getUpgradeLevels()), false, 0);

        event.setStartCooldownAfterAction(true);
    }

    @CustomEventHandler
    public void onLoreItemsCalculate(CustomItemLoreCalculateEvent event) {
        LanguageLookup lang = event.getLanguageLookup();
        List<String> lore = event.getLore();

        float power = (float) explosionPower.calculate(event.getVariables());

        lore.add(lang.getLocale("explosion_wand.power_lore", power));
    }
}
