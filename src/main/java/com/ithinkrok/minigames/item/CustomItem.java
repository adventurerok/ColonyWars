package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.oldmccw.playerclass.items.ClassItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 02/01/16.
 * <p>
 * An item with custom use or inventory click listeners
 */
public class CustomItem<U extends User> implements Identifiable{

    private static int customItemCount = 0;

    private int customItemId = customItemCount++;

    private UserInteractEvent.InteractAction<U> rightClickAction;
    private UserInteractEvent.InteractAction<U> leftClickAction;
    private ClassItem.TimeoutAction timeoutAction;
    private UserAttackEvent.AttackAction<U> attackAction;

    private String itemDisplayLocale;
    private Material itemMaterial;
    private String rightClickCooldownFinishedLocale;
    private Calculator rightClickCooldown;
    private String timeoutAbility;
    private String timeoutDescriptionLocale;
    private String timeoutFinishedLocale;
    private Calculator timeoutCalculator;
    private String rightClickCooldownAbility;
    private String descriptionLocale;

    public CustomItem(ConfigurationSection config) {

    }


    @Override
    public int getIdentifier() {
        return customItemId;
    }

}
