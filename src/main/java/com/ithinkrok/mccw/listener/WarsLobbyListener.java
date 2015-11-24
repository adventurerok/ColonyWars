package com.ithinkrok.mccw.listener;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.lobby.LobbyMinigame;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
        event.setJoinMessage(plugin.getLocale("server.players.join", name, online, max));


    }

    private ChatColor getPlayerNameColor(Player player) {
        return player.isOp() ? ChatColor.DARK_RED : ChatColor.YELLOW;
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.WRITTEN_BOOK) return;
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
        if (event.getItem() != null && event.getItem().getType() == Material.WRITTEN_BOOK) return;
        event.setCancelled(true);

        User user = plugin.getUser(event.getPlayer());

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            for (LobbyMinigame minigame : plugin.getLobbyMinigames()) {
                minigame.onUserInteractWorld(user, event.getClickedBlock());
            }
        }

        if (event.getItem() == null || !event.getItem().hasItemMeta() ||
                !event.getItem().getItemMeta().hasDisplayName()) return;

        if (plugin.getLocale("lobby.chooser.team.name").equals(event.getItem().getItemMeta().getDisplayName())) {
            showTeamChooser(event.getPlayer());
        } else if (plugin.getLocale("lobby.chooser.class.name")
                .equals(event.getItem().getItemMeta().getDisplayName())) {
            showClassChooser(event.getPlayer());
        } else if (plugin.getLocale("lobby.chooser.map.name").equals(event.getItem().getItemMeta().getDisplayName())) {
            showMapChooser(event.getPlayer());
        }
    }

    private void showTeamChooser(Player player) {
        int size = 9 * ((TeamColor.values().size() + 9) / 9);
        Inventory shopInv = Bukkit.createInventory(player, size, plugin.getLocale("lobby.chooser.team.name"));

        for (TeamColor team : TeamColor.values()) {
            shopInv.addItem(InventoryUtils.createItemWithNameAndLore(Material.WOOL, 1, team.getDyeColor().getWoolData(),
                    plugin.getLocale("team.name", team.getFormattedName())));
        }

        player.openInventory(shopInv);
    }

    private void showClassChooser(Player player) {
        Inventory shopInv = Bukkit.createInventory(player, 18, plugin.getLocale("lobby.chooser.class.name"));

        for (PlayerClass playerClass : PlayerClass.values()) {
            shopInv.addItem(InventoryUtils
                    .createItemWithNameAndLore(playerClass.getChooser(), 1, 0, playerClass.getFormattedName(),
                            plugin.getLocale("classes." + playerClass.getName() + ".desc")));
        }

        player.openInventory(shopInv);
    }

    private void showMapChooser(Player player) {
        Inventory shopInv = Bukkit.createInventory(player, 9, plugin.getLocale("lobby.chooser.map.name"));

        for (String map : plugin.getMapList()) {
            shopInv.addItem(InventoryUtils
                    .createItemWithNameAndLore(Material.MAP, 1, 0, map, plugin.getLocale("maps." + map + ".desc")));
        }

        player.openInventory(shopInv);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        for (LobbyMinigame minigame : plugin.getLobbyMinigames()) {
            if (minigame.onUserInteractEntity(plugin.getUser(event.getPlayer()), event.getRightClicked())) break;
        }
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

            if (plugin.getLocale("lobby.chooser.team.name").equals(event.getInventory().getTitle())) {
                TeamColor teamColor = TeamColor.fromWoolColor(event.getCurrentItem().getDurability());
                if (teamColor == null) {
                    user.message("Null team. This is impossible error");
                    return;
                }

                if (teamColor == user.getTeamColor()) {
                    user.message(plugin.getLocale("team.join.already-member", teamColor.getFormattedName()));
                    return;
                }

                int playerCount = plugin.getPlayerCount();
                int teamSize = plugin.getTeam(teamColor).getPlayerCount();

                if (teamSize >= (playerCount + TeamColor.values().size() - 1) / TeamColor.values().size()) {
                    user.message(plugin.getLocale("team.join.full", teamColor.getFormattedName()));
                    return;
                }

                user.setTeamColor(teamColor);

                user.message(plugin.getLocale("team.join.success", teamColor.getFormattedName()));

                user.getPlayer().closeInventory();
            } else if (plugin.getLocale("lobby.chooser.class.name").equals(event.getInventory().getTitle())) {
                PlayerClass playerClass = PlayerClass.fromChooserMaterial(event.getCurrentItem().getType());
                if (playerClass == null) {
                    user.message("Null class. This is impossible error");
                    return;
                }

                user.setPlayerClass(playerClass);

                user.message(plugin.getLocale("class.join.success", playerClass.getFormattedName()));

                user.getPlayer().closeInventory();
            } else if (plugin.getLocale("lobby.chooser.map.name").equals(event.getInventory().getTitle())) {
                String mapName = event.getCurrentItem().getItemMeta().getDisplayName();

                String oldVote = user.getMapVote();

                if (mapName.equals(oldVote)) {
                    user.message(plugin.getLocale("voting.maps.already-voted", mapName));
                    return;
                }

                user.setMapVote(mapName);
                user.message(plugin.getLocale("voting.maps.success", mapName));

                String playerName = getPlayerNameColor(user.getPlayer()) + user.getFormattedName();

                if (oldVote == null) {
                    plugin.messageAll(plugin.getLocale("voting.maps.player-voted", playerName, mapName));
                } else {
                    plugin.messageAll(plugin.getLocale("voting.maps.player-transfer", playerName, oldVote, mapName));
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

        for (LobbyMinigame minigame : plugin.getLobbyMinigames()) {
            minigame.onUserQuitLobby(user);
        }

        String name = getPlayerNameColor(event.getPlayer()) + event.getPlayer().getName();
        String online = Integer.toString(plugin.getPlayerCount() - 1);
        String max = Integer.toString(plugin.getServer().getMaxPlayers());
        event.setQuitMessage(plugin.getLocale("server.players.quit", name, online, max));
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);


        ((Player) event.getEntity()).setSaturation(20);
    }
}
