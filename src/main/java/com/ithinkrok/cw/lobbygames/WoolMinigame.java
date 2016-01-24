package com.ithinkrok.cw.lobbygames;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.EntityUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Random;

/**
 * Created by paul on 23/01/16.
 */
public class WoolMinigame implements Listener {

    private final Random random = new Random();

    private User woolUser;

    @MinigamesEventHandler
    public void onUserJoin(UserJoinEvent event) {
        if (woolUser != null) return;

        woolUser = event.getUser();
        giveInitialWool();
    }

    public void giveInitialWool() {
        giveWoolHelmet();

        woolUser.getGameGroup().sendLocale("wool_head.initial", woolUser.getFormattedName());
        woolUser.sendLocale("wool_head.given");
    }

    private void giveWoolHelmet() {
        woolUser.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
    }

    @MinigamesEventHandler
    public void onUserQuit(UserQuitEvent event) {
        if (!Objects.equals(woolUser, event.getUser())) return;

        GameGroup gameGroup = event.getUserGameGroup();

        if (gameGroup.getUserCount() <= 1) {
            woolUser = null;
            return;
        }

        while (Objects.equals(woolUser, event.getUser())) {
            int index = random.nextInt(gameGroup.getUserCount());

            for (User next : gameGroup.getUsers()) {
                if (index == 0) {
                    woolUser = next;
                    break;
                }
                --index;
            }
        }

        giveInitialWool();
    }

    @MinigamesEventHandler
    public void onUserInteract(UserInteractEvent event) {
        if (!event.hasEntity()) return;

        if (!Objects.equals(woolUser, event.getUser()) || !(event.getClickedEntity() instanceof Player)) return;

        User newWool = EntityUtils.getActualUser(woolUser, event.getClickedEntity());
        if (newWool == null) return;

        User oldWool = woolUser;
        woolUser = newWool;

        oldWool.getInventory().setHelmet(null);
        giveWoolHelmet();

        woolUser.getGameGroup()
                .sendLocale("wool_head.transfer", oldWool.getFormattedName(), woolUser.getFormattedName());
    }
}
