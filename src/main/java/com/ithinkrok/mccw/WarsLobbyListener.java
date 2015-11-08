package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 08/11/15.
 *
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
    public void onPickupItem(PlayerPickupItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event){
        event.setCancelled(true);
    }
}
