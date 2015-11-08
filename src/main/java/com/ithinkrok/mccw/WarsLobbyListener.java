package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

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
        PlayerInventory inv = event.getPlayer().getInventory();

        inv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.LEATHER_HELMET, 1, 0, "Team Chooser", "Choose your team"));

        inv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.WOOD_SWORD, 1, 0, "Class Chooser", "Choose your class"));

        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        playerInfo.message(ChatColor.GREEN + "Choose a team or class or you will be assigned one automatically");

        playerInfo.message(ChatColor.GREEN + "Canyon is the only map so there is no map voting!");

        playerInfo.getPlayer().setGameMode(GameMode.ADVENTURE);
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

        switch (event.getItem().getItemMeta().getDisplayName()) {
            case "Team Chooser":
                showTeamChooser(event.getPlayer());
                break;
            case "Class Chooser":
                showClassChooser(event.getPlayer());
                break;
        }
    }

    private void showTeamChooser(Player player) {
        Inventory shopInv = Bukkit.createInventory(player, 9, "Team Chooser");

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.WOOL, 1, TeamColor.RED.dyeColor.getWoolData(), "Red Team"));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.WOOL, 1, TeamColor.GREEN.dyeColor.getWoolData(), "Green Team"));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.WOOL, 1, TeamColor.YELLOW.dyeColor.getWoolData(), "Yellow Team"));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.WOOL, 1, TeamColor.BLUE.dyeColor.getWoolData(), "Blue Team"));

        player.openInventory(shopInv);
    }

    private void showClassChooser(Player player) {
        Inventory shopInv = Bukkit.createInventory(player, 9, "Class Chooser");

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_SWORD, 1, 0, "General", "Fights with a diamond sword"));

        shopInv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.COMPASS, 1, 0, "Scout", "Runs fast and has a player locator"));

        shopInv.addItem(InventoryUtils.createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloaker",
                "Can go invisible for short periods of time"));

        player.openInventory(shopInv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
