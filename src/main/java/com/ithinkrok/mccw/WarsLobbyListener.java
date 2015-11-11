package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

/**
 * Created by paul on 08/11/15.
 * <p>
 * Handles bukkit events in the lobby
 */
public class WarsLobbyListener implements Listener {

    private final WarsPlugin plugin;

    public WarsLobbyListener(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.playerJoinLobby(event.getPlayer());
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        event.setCancelled(true);

        if (event.getItem() == null || !event.getItem().hasItemMeta() ||
                !event.getItem().getItemMeta().hasDisplayName()) return;

        if(plugin.getLocale("team-chooser").equals(event.getItem().getItemMeta().getDisplayName())){
            showTeamChooser(event.getPlayer());
        } else if(plugin.getLocale("class-chooser").equals(event.getItem().getItemMeta().getDisplayName())){
            showClassChooser(event.getPlayer());
        }
    }

    private void showTeamChooser(Player player) {
        Inventory shopInv = Bukkit.createInventory(player, 9, plugin.getLocale("team-chooser"));

        for (TeamColor team : TeamColor.values()) {
            shopInv.addItem(InventoryUtils.createItemWithNameAndLore(Material.WOOL, 1, team.dyeColor.getWoolData(),
                    plugin.getLocale("team-desc", team.name)));
        }

        player.openInventory(shopInv);
    }

    private void showClassChooser(Player player) {
        Inventory shopInv = Bukkit.createInventory(player, 9, plugin.getLocale("class-chooser"));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_SWORD, 1, 0, "General", plugin.getLocale("general-desc")));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.COMPASS, 1, 0, "Scout", plugin.getLocale("scout-desc")));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.BOW, 1, 0, "Archer", plugin.getLocale("archer-desc")));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloaker", plugin.getLocale("cloaker-desc")));

        player.openInventory(shopInv);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setFormat(
                ChatColor.DARK_GRAY + "<" + ChatColor.WHITE + "%s" + ChatColor.DARK_GRAY + "> " + ChatColor.WHITE +
                        "%s");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getInventory().getTitle() == null) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() ||
                !event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        String item = event.getCurrentItem().getItemMeta().getDisplayName();

        PlayerInfo playerInfo = plugin.getPlayerInfo((Player) event.getWhoClicked());

        try {

            if (plugin.getLocale("team-chooser").equals(event.getInventory().getTitle())) {
                String teamName = item.substring(0, item.length() - 5).toUpperCase();
                TeamColor teamColor = TeamColor.valueOf(teamName);

                if (teamColor == playerInfo.getTeamColor()) {
                    playerInfo.message(plugin.getLocale("team-already-member", teamColor.name));
                    return;
                }

                int playerCount = plugin.getPlayerCount();
                int teamSize = plugin.getTeamInfo(teamColor).getPlayerCount();

                if (teamSize >= (playerCount + 3) / 4) {
                    playerInfo.message(plugin.getLocale("team-full", teamColor.name));
                    return;
                }

                plugin.setPlayerTeam(playerInfo.getPlayer(), teamColor);

                playerInfo.message(plugin.getLocale("team-joined", teamColor.name));

                playerInfo.getPlayer().closeInventory();
            } else if (plugin.getLocale("class-chooser").equals(event.getInventory().getTitle())) {
                String className = item.toUpperCase();
                PlayerClass playerClass = PlayerClass.valueOf(className);

                playerInfo.setPlayerClass(playerClass);

                playerInfo.message(plugin.getLocale("class-selected", playerClass.name));

                playerInfo.getPlayer().closeInventory();
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.setPlayerTeam(event.getPlayer(), null);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);


        ((Player) event.getEntity()).setSaturation(20);
    }
}
