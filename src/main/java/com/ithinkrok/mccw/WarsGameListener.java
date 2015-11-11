package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.SchematicData;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.Facing;
import com.ithinkrok.mccw.util.SchematicBuilder;
import com.ithinkrok.mccw.util.TreeFeller;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 01/11/15.
 * <p>
 * Listens for Bukkit events while the game is in progress
 */
public class WarsGameListener implements Listener {

    private static HashMap<PotionEffectType, Boolean> GOOD_POTIONS = new HashMap<>();

    static {
        GOOD_POTIONS.put(PotionEffectType.HEAL, true);
        GOOD_POTIONS.put(PotionEffectType.HARM, false);

        //Add more potions if required
    }

    private WarsPlugin plugin;

    public WarsGameListener(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());
        if (playerInfo == null) {
            event.setCancelled(true);
            return;
        }

        if (playerInfo.isInGame() && playerInfo.getTeamColor() != null) {
            String playerClass = playerInfo.getPlayerClass().toString();
            String teamColor = playerInfo.getTeamColor().chatColor.toString();
            event.setFormat(teamColor + "<" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + playerClass +
                    ChatColor.DARK_GRAY + "] %s" + teamColor + "> " + ChatColor.WHITE + "%s");
        } else {
            event.setFormat(ChatColor.LIGHT_PURPLE + "<" + ChatColor.GRAY + "%s" + ChatColor.LIGHT_PURPLE + "> " +
                    ChatColor.WHITE + "%s");
        }

        plugin.getPlayers().stream()
                .filter(other -> other.getTeamColor() != playerInfo.getTeamColor() && !other.getPlayer().isOp())
                .forEach(other -> event.getRecipients().remove(other.getPlayer()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        setSpectator(playerInfo.getPlayer());
        playerInfo.getPlayer().teleport(plugin.getMapSpawn(null));

        playerInfo.updateScoreboard();

        playerInfo.message(plugin.getLocale("game-in-progress"));
        playerInfo.message(plugin.getLocale("game-wait-next"));
        playerInfo.message(plugin.getLocale("spectate-heads"));
    }

    public void setSpectator(Player died) {
        plugin.setPlayerTeam(died, null);
        plugin.getPlayerInfo(died).setInGame(false);
        plugin.cloak(died);
        died.setAllowFlight(true);
        died.setMaxHealth(plugin.getMaxHealth());
        died.setHealth(plugin.getMaxHealth());

        plugin.setupSpectatorInventory(died);
        plugin.getPlayerInfo(died).clearArmor();

        plugin.removePotionEffects(died);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }

        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (!playerInfo.isInGame()) {
            event.setCancelled(true);
            return;
        }

        switch (event.getItem().getItemStack().getType()) {
            case GOLD_INGOT:
                giveCashPerItem(event, 120, 80);
                break;
            case DIAMOND:
                giveCashPerItem(event, 1200, 800);
                break;
        }

        event.setCancelled(true);
        event.getItem().remove();
    }

    private void giveCashPerItem(PlayerPickupItemEvent event, int playerCash, int teamCash) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        playerInfo.addPlayerCash(playerCash * event.getItem().getItemStack().getAmount());
        event.getPlayer().playSound(event.getItem().getLocation(), Sound.ORB_PICKUP, 1.0f,
                0.8f + (plugin.getRandom().nextFloat()) * 0.4f);

        TeamInfo teamInfo = plugin.getTeamInfo(playerInfo.getTeamColor());
        teamInfo.addTeamCash(teamCash * event.getItem().getItemStack().getAmount());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (!playerInfo.isInGame()) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() != Material.LAPIS_ORE) return;

        ItemMeta meta = event.getItemInHand().getItemMeta();
        if (!meta.hasDisplayName()) return;

        SchematicData schematicData = plugin.getSchematicData(meta.getDisplayName());
        if (schematicData == null) {
            playerInfo.message(ChatColor.RED + "Unknown building!");
            event.setCancelled(true);
            return;
        }

        int rotation = Facing.getFacing(event.getPlayer().getLocation().getYaw());

        if (!SchematicBuilder.buildSchematic(plugin, schematicData, event.getBlock().getLocation(), rotation,
                playerInfo.getTeamColor())) {
            event.setCancelled(true);
            playerInfo.message(ChatColor.RED + "You cannot build that here!");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (!playerInfo.isInGame()) {
            event.setCancelled(true);
            return;
        }

        resetDurability(event.getPlayer());

        if (event.getBlock().getType() != Material.OBSIDIAN) return;

        BuildingInfo buildingInfo = plugin.getBuildingInfo(event.getBlock().getLocation());
        if (buildingInfo == null) {
            plugin.getLogger().warning("The player destroyed an obsidian block, but it wasn't a building. Odd");
            plugin.getLogger().warning("Obsidian location: " + event.getBlock().getLocation());
            playerInfo.message(ChatColor.RED + "That obsidian block does not appear to be part of a building");
            return;
        }

        if (playerInfo.getTeamColor() == buildingInfo.getTeamColor()) {
            playerInfo.message(ChatColor.RED + "You cannot destroy your own team's buildings!");
            event.setCancelled(true);
            return;
        }

        if (Buildings.BASE.equals(buildingInfo.getBuildingName())) {
            playerInfo.message(ChatColor.RED + "You cannot destroy other team's bases!");
            return;
        }

        buildingInfo.explode();
    }

    private void resetDurability(Player player) {
        ItemStack item = player.getItemInHand();

        if (item == null) return;
        if (item.getDurability() != 0 && item.getType().getMaxDurability() != 0) {
            item.setDurability((short) 0);
            player.setItemInHand(item);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExp(BlockExpEvent event) {
        switch (event.getBlock().getType()) {
            case GOLD_ORE:
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, 3));
                break;
            case LOG:
            case LOG_2:
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, 1));
                TreeFeller.fellTree(event.getBlock().getLocation());
                break;
            case DIAMOND_ORE:
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.DIAMOND, 1));
        }

        event.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        event.getBrokenItem().setDurability((short) 0);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (!playerInfo.isInGame()) {
            event.setCancelled(true);
            return;
        }

        resetDurability(event.getPlayer());

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.OBSIDIAN) {
            PlayerClassHandler classHandler = plugin.getPlayerClassHandler(playerInfo.getPlayerClass());

            classHandler.onInteractWorld(event);
            return;
        }

        BuildingInfo buildingInfo = plugin.getBuildingInfo(event.getClickedBlock().getLocation());
        if (buildingInfo == null) {
            plugin.getLogger().warning("The player destroyed an obsidian block, but it wasn't a building. Odd");
            plugin.getLogger().warning("Obsidian location: " + event.getClickedBlock().getLocation());
            playerInfo.message(ChatColor.RED + "That obsidian block does not appear to be part of a building.");
            return;
        }

        if (playerInfo.getTeamColor() != buildingInfo.getTeamColor()) {
            playerInfo.message("That building does not belong to your team." + ChatColor.BOLD +
                    "Mine this block to destroy it!");
            return;
        }

        if (!buildingInfo.isFinished()) {
            playerInfo.message(ChatColor.RED + "You must wait until the building has finished construction.");
            return;
        }

        event.setCancelled(true);

        playerInfo.setShopBlock(buildingInfo.getCenterBlock());

        Inventory shopInv = Bukkit.createInventory(event.getPlayer(), 9, buildingInfo.getBuildingName());

        TeamInfo teamInfo = plugin.getTeamInfo(playerInfo.getTeamColor());

        InventoryHandler inventoryHandler = plugin.getBuildingInventoryHandler();
        List<ItemStack> contents = new ArrayList<>();

        if (inventoryHandler != null) inventoryHandler.addInventoryItems(contents, buildingInfo, playerInfo, teamInfo);

        PlayerClassHandler classHandler = plugin.getPlayerClassHandler(playerInfo.getPlayerClass());
        classHandler.addInventoryItems(contents, buildingInfo, playerInfo, teamInfo);

        int index = 0;

        for (ItemStack item : contents) {
            shopInv.setItem(index++, item);
        }

        event.getPlayer().openInventory(shopInv);
        playerInfo.setShopInventory(shopInv);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.isInShowdown()) return;
        if (event.getPlayer().getAllowFlight()) return;

        if (!plugin.isInShowdownBounds(event.getTo())) {
            if (!plugin.isInShowdownBounds(event.getFrom())) {
                event.getPlayer().teleport(plugin.getMapSpawn(null));
                plugin.messageAll(event.getPlayer().getDisplayName() + ChatColor.GOLD + " was teleported back to the " +
                        "center!");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        resetDurability((Player) event.getEntity());
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player damager;
        if (!(event.getDamager() instanceof Player)) {
            if (event.getDamager() instanceof Projectile) {
                Projectile arrow = (Projectile) event.getDamager();

                if (!(arrow.getShooter() instanceof Player)) return;
                damager = (Player) arrow.getShooter();
            } else return;
        } else {
            damager = (Player) event.getDamager();
        }

        PlayerInfo damagerInfo = plugin.getPlayerInfo(damager);

        if (!damagerInfo.isInGame()) {
            event.setCancelled(true);
            return;
        }

        resetDurability(damager);

        if (!(event.getEntity() instanceof Player)) return;

        Player target = (Player) event.getEntity();

        if (damagerInfo.getTeamColor() == plugin.getPlayerInfo(target).getTeamColor()) {
            event.setCancelled(true);
            return;
        }

        if (target.getHealth() - event.getDamage() < 1) {
            event.setCancelled(true);
            playerDeath(target, damager);
        }
    }

    public void playerDeath(Player died, Player killer) {
        if (plugin.isInAftermath()) return;
        PlayerInfo diedInfo = plugin.getPlayerInfo(died);

        if (!diedInfo.isInGame()) {
            died.setMaxHealth(plugin.getMaxHealth());
            died.setHealth(plugin.getMaxHealth());

            died.teleport(plugin.getMapSpawn(null));
            return;
        }

        PlayerInfo killerInfo = killer == null ? null : plugin.getPlayerInfo(killer);
        if (killerInfo != null) {
            plugin.messageAll(ChatColor.GOLD + diedInfo.getFormattedName() + ChatColor.GOLD +
                    " was killed by " + killerInfo.getFormattedName());
        } else {
            plugin.messageAll(ChatColor.GOLD + diedInfo.getFormattedName() + ChatColor.GOLD + " died!");
        }

        TeamInfo diedTeam = plugin.getTeamInfo(diedInfo.getTeamColor());
        boolean respawn =
                !plugin.isInShowdown() && plugin.getRandom().nextFloat() < (diedTeam.getRespawnChance() / 100f);

        if (respawn) {
            plugin.messageAll(diedInfo.getFormattedName() + ChatColor.GOLD + " has respawned!");

            diedTeam.setRespawnChance(diedTeam.getRespawnChance() - 15);
            diedTeam.respawnPlayer(died);

            died.setHealth(40);
            died.setSaturation(5);
            died.setFoodLevel(20);

            diedTeam.message(
                    ChatColor.GOLD + "Your revival chance is now " + ChatColor.DARK_AQUA + diedTeam.getRespawnChance());
        } else {
            plugin.messageAll(diedInfo.getFormattedName() + ChatColor.GOLD + " did not respawn!");
            teamPlayerDied(diedInfo, diedTeam);

            setSpectator(died);
        }
    }

    public void teamPlayerDied(PlayerInfo diedInfo, TeamInfo diedTeam) {
        plugin.setPlayerTeam(diedInfo.getPlayer(), null);

        plugin.messageAll(ChatColor.GOLD + "The " + diedTeam.getTeamColor().name + ChatColor.GOLD +
                " has lost a player!");
        plugin.messageAll(ChatColor.GOLD + "There are now " + ChatColor.DARK_AQUA + diedTeam.getPlayerCount() +
                ChatColor.GOLD + " players left on the " + diedTeam.getTeamColor().name + ChatColor.GOLD + " Team");

        if (diedTeam.getPlayerCount() == 0) {
            diedTeam.eliminate();
        }

        plugin.checkVictory(true);

        for(PlayerInfo info : plugin.getPlayers()){
            if(info.isInGame() && info.getTeamColor() != null) return;

            plugin.setupSpectatorInventory(info.getPlayer());
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ProjectileSource projectileSource = event.getEntity().getShooter();

        if (!(projectileSource instanceof Player)) return;
        Player shooter = (Player) projectileSource;
        PlayerInfo shooterInfo = plugin.getPlayerInfo(shooter);

        if (!shooterInfo.isInGame() || shooterInfo.getTeamColor() == null) {
            event.setCancelled(true);
            return;
        }

        //If NPE then add potion to GOOD_POTIONS
        boolean good = GOOD_POTIONS.get(event.getPotion().getEffects().iterator().next().getType());

        for (LivingEntity ent : event.getAffectedEntities()) {
            if (!(ent instanceof Player)) continue;

            Player player = (Player) ent;
            PlayerInfo playerInfo = plugin.getPlayerInfo(player);

            if (!playerInfo.isInGame() || playerInfo.getTeamColor() == null ||
                    (playerInfo.getTeamColor() == shooterInfo.getTeamColor()) != good) {
                event.setIntensity(ent, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player target = (Player) event.getEntity();

        if (target.getHealth() - event.getDamage() < 1) {
            event.setCancelled(true);
            playerDeath(target, null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerInfo diedInfo = plugin.getPlayerInfo(event.getPlayer());
        TeamInfo diedTeam = plugin.getTeamInfo(diedInfo.getTeamColor());
        if (diedTeam == null) return;

        teamPlayerDied(diedInfo, diedTeam);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType().equals(InventoryType.SlotType.ARMOR)) event.setCancelled(true);

        PlayerInfo playerInfo = plugin.getPlayerInfo((Player) event.getWhoClicked());
        if(!playerInfo.isInGame() || playerInfo.getTeamColor() == null){
            plugin.handleSpectatorInventory(event);
            return;
        }

        if (event.getInventory().getType() != InventoryType.PLAYER &&
                event.getInventory().getType() != InventoryType.CRAFTING) {

            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;


            playerInfo.setShopInventory(event.getInventory());

            TeamInfo teamInfo = plugin.getTeamInfo(playerInfo.getTeamColor());

            PlayerClassHandler classHandler = plugin.getPlayerClassHandler(playerInfo.getPlayerClass());

            BuildingInfo buildingInfo = plugin.getBuildingInfo(playerInfo.getShopBlock());
            if (buildingInfo == null) return;

            boolean done = classHandler.onInventoryClick(event.getCurrentItem(), buildingInfo, playerInfo, teamInfo);

            if (done) return;

            InventoryHandler handler = plugin.getBuildingInventoryHandler();
            if (handler == null) return;

            handler.onInventoryClick(event.getCurrentItem(), buildingInfo, playerInfo, teamInfo);
        }
    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        PlayerInfo playerInfo = plugin.getPlayerInfo(player);

        if (playerInfo.getShopInventory() == null || playerInfo.getShopInventory().getTitle() == null) return;

        if (playerInfo.getShopInventory().getTitle().equals(event.getInventory().getTitle())) {
            playerInfo.setShopInventory(null);
            playerInfo.setShopBlock(null);
        }
    }


}
