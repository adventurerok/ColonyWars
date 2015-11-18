package com.ithinkrok.mccw.listener;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.handler.LobbyMinigame;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.playerJoinLobby(event.getPlayer());

        String name = getPlayerNameColor(event.getPlayer()) + event.getPlayer().getName();
        String online = Integer.toString(plugin.getPlayerCount());
        String max = Integer.toString(plugin.getServer().getMaxPlayers());
        event.setJoinMessage(plugin.getLocale("player-join-game", name, online, max));


    }

    private ChatColor getPlayerNameColor(Player player) {
        return player.isOp() ? ChatColor.DARK_RED : ChatColor.YELLOW;
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if(event.getItem().getItemStack().getType() == Material.WRITTEN_BOOK) return;
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
        if(event.getItem() != null && event.getItem().getType() == Material.WRITTEN_BOOK) return;
        event.setCancelled(true);

        if (event.getItem() == null || !event.getItem().hasItemMeta() ||
                !event.getItem().getItemMeta().hasDisplayName()) return;

        if (plugin.getLocale("team-chooser").equals(event.getItem().getItemMeta().getDisplayName())) {
            showTeamChooser(event.getPlayer());
        } else if (plugin.getLocale("class-chooser").equals(event.getItem().getItemMeta().getDisplayName())) {
            showClassChooser(event.getPlayer());
        } else if(plugin.getLocale("map-chooser").equals(event.getItem().getItemMeta().getDisplayName())){
            showMapChooser(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
        for(LobbyMinigame minigame : plugin.getLobbyMinigames()){
            if(minigame.onUserInteractEntity(plugin.getUser(event.getPlayer()), event.getRightClicked())) break;
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
        Inventory shopInv = Bukkit.createInventory(player, 18, plugin.getLocale("class-chooser"));

        for (PlayerClass playerClass : PlayerClass.values()) {
            shopInv.addItem(InventoryUtils.createItemWithNameAndLore(playerClass.chooser, 1, 0, playerClass.name,
                    plugin.getLocale(playerClass.toString().toLowerCase() + "-desc")));
        }

        player.openInventory(shopInv);
    }

    private void showMapChooser(Player player) {
        Inventory shopInv = Bukkit.createInventory(player, 9, plugin.getLocale("map-chooser"));

        for (String map : plugin.getMapList()) {
            shopInv.addItem(
                    InventoryUtils.createItemWithNameAndLore(Material.MAP, 1, 0, map, plugin.getLocale(map + "-desc")));
        }

        player.openInventory(shopInv);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        ChatColor playerColor = getPlayerNameColor(event.getPlayer());

        event.setFormat(ChatColor.DARK_GRAY + "<" + playerColor + "%s" + ChatColor.DARK_GRAY + "> " + ChatColor.WHITE +
                "%s");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getInventory().getTitle() == null) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() ||
                !event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        User user = plugin.getUser((Player) event.getWhoClicked());

        try {

            if (plugin.getLocale("team-chooser").equals(event.getInventory().getTitle())) {
                TeamColor teamColor = TeamColor.fromWoolColor(event.getCurrentItem().getDurability());
                if (teamColor == null) {
                    user.message("Null team. This is impossible error");
                    return;
                }

                if (teamColor == user.getTeamColor()) {
                    user.message(plugin.getLocale("team-already-member", teamColor.name));
                    return;
                }

                int playerCount = plugin.getPlayerCount();
                int teamSize = plugin.getTeam(teamColor).getPlayerCount();

                if (teamSize >= (playerCount + 3) / 4) {
                    user.message(plugin.getLocale("team-full", teamColor.name));
                    return;
                }

                user.setTeamColor(teamColor);

                user.message(plugin.getLocale("team-joined", teamColor.name));

                user.getPlayer().closeInventory();
            } else if (plugin.getLocale("class-chooser").equals(event.getInventory().getTitle())) {
                PlayerClass playerClass = PlayerClass.fromChooserMaterial(event.getCurrentItem().getType());
                if (playerClass == null) {
                    user.message("Null class. This is impossible error");
                    return;
                }

                user.setPlayerClass(playerClass);

                user.message(plugin.getLocale("class-selected", playerClass.name));

                user.getPlayer().closeInventory();
            } else if (plugin.getLocale("map-chooser").equals(event.getInventory().getTitle())) {
                String mapName = event.getCurrentItem().getItemMeta().getDisplayName();

                String oldVote = user.getMapVote();

                if (mapName.equals(oldVote)) {
                    user.message(plugin.getLocale("map-already-voted", mapName));
                    return;
                }

                user.setMapVote(mapName);
                user.message(plugin.getLocale("map-voted", mapName));

                String playerName = getPlayerNameColor(user.getPlayer()) + user.getFormattedName();

                if (oldVote == null) {
                    plugin.messageAll(plugin.getLocale("player-voted", playerName, mapName));
                } else {
                    plugin.messageAll(plugin.getLocale("player-vote-change", playerName, oldVote, mapName));
                }

                user.getPlayer().closeInventory();
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        User user = plugin.getUser(event.getPlayer());
        user.setTeamColor(null);
        user.setMapVote(null);

        for(LobbyMinigame minigame : plugin.getLobbyMinigames()){
            minigame.onUserQuitLobby(user);
        }

        String name = getPlayerNameColor(event.getPlayer()) + event.getPlayer().getName();
        String online = Integer.toString(plugin.getPlayerCount() - 1);
        String max = Integer.toString(plugin.getServer().getMaxPlayers());
        event.setQuitMessage(plugin.getLocale("player-quit-game", name, online, max));
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);


        ((Player) event.getEntity()).setSaturation(20);
    }
}
