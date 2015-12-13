package com.ithinkrok.mccw.listener;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.command.executors.FixExecutor;
import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.Schematic;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.GameState;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserAttackedEvent;
import com.ithinkrok.mccw.event.UserInteractWorldEvent;
import com.ithinkrok.mccw.event.UserRightClickEntityEvent;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.Disguises;
import com.ithinkrok.mccw.util.EntityUtils;
import com.ithinkrok.mccw.util.TreeFeller;
import com.ithinkrok.mccw.util.building.Facing;
import com.ithinkrok.mccw.util.building.SchematicBuilder;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 01/11/15.
 * <p>
 * Listens for Bukkit events while the game is in progress
 */
public class WarsGameListener implements Listener {

    private static Map<PotionEffectType, Boolean> GOOD_POTIONS = new HashMap<>();
    private static Map<EntityDamageEvent.DamageCause, String> DEATH_MESSAGE_ENDINGS =
            new EnumMap<>(EntityDamageEvent.DamageCause.class);

    static {
        GOOD_POTIONS.put(PotionEffectType.HEAL, true);
        GOOD_POTIONS.put(PotionEffectType.HARM, false);
        GOOD_POTIONS.put(PotionEffectType.INCREASE_DAMAGE, true);
        //Add more potions if required

        DEATH_MESSAGE_ENDINGS.put(EntityDamageEvent.DamageCause.FALL, ".falling");
        DEATH_MESSAGE_ENDINGS.put(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, ".explosion");
        DEATH_MESSAGE_ENDINGS.put(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, ".explosion");
        DEATH_MESSAGE_ENDINGS.put(EntityDamageEvent.DamageCause.PROJECTILE, ".shot");
        DEATH_MESSAGE_ENDINGS.put(EntityDamageEvent.DamageCause.DROWNING, ".drowned");
        DEATH_MESSAGE_ENDINGS.put(EntityDamageEvent.DamageCause.FIRE, ".fire");
        DEATH_MESSAGE_ENDINGS.put(EntityDamageEvent.DamageCause.FIRE_TICK, ".fire");


    }

    private WarsPlugin plugin;

    public WarsGameListener(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        User user = plugin.getUser(event.getPlayer());
        if (user == null) {
            event.setCancelled(true);
            return;
        }

        if (user.isInGame() && user.getTeamColor() != null) {
            String playerClass = user.getPlayerClass().getName().toUpperCase();
            String teamColor = user.getTeamColor().getChatColor().toString();
            event.setFormat(teamColor + "<" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + playerClass +
                    ChatColor.DARK_GRAY + "] %s" + teamColor + "> " + ChatColor.WHITE + "%s");
        } else {
            event.setFormat(ChatColor.LIGHT_PURPLE + "<" + ChatColor.GRAY + "%s" + ChatColor.LIGHT_PURPLE + "> " +
                    ChatColor.WHITE + "%s");
        }

        //        plugin.getUsers().stream()
        //                .filter(other -> other.getTeamColor() != user.getTeamColor() && !other.getPlayer().isOp())
        //                .forEach(other -> event.getRecipients().remove(other.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = plugin.getUser(event.getPlayer());

        user.getPlayer().teleport(plugin.getMapSpawn(null));
        user.setSpectator();

        user.sendLocale("spectators.game.in-progress");
        user.sendLocale("spectators.game.wait-next");
        user.sendLocale("spectators.players.chose");

        //Set allow flight to true after essentials has the opportunity to set it to false
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, user::setSpectator, 2);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        switch (event.getEntity().getItemStack().getType()) {
            case GOLD_INGOT:
            case DIAMOND:
                return;
            default:
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType().isEdible()) return;

        switch (event.getItemDrop().getItemStack().getType()) {
            case POTION:
            case TNT:
            case WRITTEN_BOOK:
                return;
        }
        event.setCancelled(true);
    }


    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }

        User user = plugin.getUser(event.getPlayer());

        if (!user.isInGame()) {
            event.setCancelled(true);
            return;
        }

        switch (event.getItem().getItemStack().getType()) {
            case GOLD_INGOT:
                giveCashPerItem(event, 60, 40);
                break;
            case DIAMOND:
                giveCashPerItem(event, 1200, 800);
                break;
        }

        event.setCancelled(true);
        event.getItem().remove();
    }

    private void giveCashPerItem(PlayerPickupItemEvent event, int playerCash, int teamCash) {
        User user = plugin.getUser(event.getPlayer());

        user.addPlayerCash(playerCash * event.getItem().getItemStack().getAmount());
        event.getPlayer().playSound(event.getItem().getLocation(), Sound.ORB_PICKUP, 1.0f,
                0.8f + (plugin.getRandom().nextFloat()) * 0.4f);

        Team team = user.getTeam();
        team.addTeamCash(teamCash * event.getItem().getItemStack().getAmount());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        User user = plugin.getUser(event.getPlayer());

        if (!user.isInGame()) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() != Material.LAPIS_ORE) return;

        ItemMeta meta = event.getItemInHand().getItemMeta();
        if (!meta.hasDisplayName()) return;

        Schematic schematic = plugin.getSchematicData(meta.getDisplayName());
        if (schematic == null) {
            user.sendLocale("building.unknown");
            event.setCancelled(true);
            return;
        }

        int rotation = Facing.getFacing(event.getPlayer().getLocation().getYaw());

        if (!SchematicBuilder
                .buildSchematic(plugin, schematic, event.getBlock().getLocation(), rotation, user.getTeamColor())) {
            event.setCancelled(true);
            user.sendLocale("building.invalid-loc");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        User user = plugin.getUser(event.getPlayer());

        if (!user.isInGame()) {
            event.setCancelled(true);
            return;
        }

        resetDurability(event.getPlayer());

        if (event.getBlock().getType() != Material.OBSIDIAN) return;

        Building building = plugin.getBuildingInfo(event.getBlock().getLocation());
        if (building == null) {
            user.sendLocale("building.destroy.invalid");
            return;
        }

        if (user.getTeamColor() == building.getTeamColor()) {
            user.sendLocale("building.destroy.own-team");
            event.setCancelled(true);
            return;
        }

        if (Buildings.BASE.equals(building.getBuildingName()) ||
                Buildings.FORTRESS.equals(building.getBuildingName())) {
            user.sendLocale("building.destroy.bases");
            event.setCancelled(true);
            return;
        }

        plugin.messageAllLocale("building.destroyed", user.getFormattedName(), building.getBuildingName(),
                building.getTeamColor().getFormattedName());
        plugin.getGameInstance().scheduleTask(building::explode, 60);
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
        if (plugin.getGameState() == GameState.AFTERMATH) return;
        switch (event.getBlock().getType()) {
            case QUARTZ_ORE:
            case GOLD_ORE:
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, 6));
                break;
            case LOG:
            case LOG_2:
                if (plugin.isInBuilding(event.getBlock().getLocation())) break;
                int count = 1 + TreeFeller.fellTree(plugin.getGameInstance(), event.getBlock().getLocation());
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> event.getBlock().getWorld()
                                .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, count)),
                        1);

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
        User user = plugin.getUser(event.getPlayer());

        if (!user.isInGame()) {
            if (event.getItem() != null && event.getItem().getType() == Material.IRON_LEGGINGS) {
                user.setShowCloakedPlayers(!user.showCloakedPlayers());
            }

            event.setCancelled(true);
            return;
        }

        resetDurability(event.getPlayer());

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || (event.getClickedBlock().getType() != Material.OBSIDIAN &&
                event.getClickedBlock().getType() != Material.ENDER_CHEST)) {
            PlayerClassHandler classHandler = user.getPlayerClassHandler();

            if (classHandler.onInteract(new UserInteractWorldEvent(user, event))) event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        switch (event.getClickedBlock().getType()) {
            case OBSIDIAN:
                user.openShopInventory(event.getClickedBlock().getLocation());
                break;
            case ENDER_CHEST:
                int amount = 5000 + plugin.getRandom().nextInt(20) * 1000;

                user.getTeam().messageLocale("ender-chest.found", user.getFormattedName());
                user.addPlayerCash(amount);

                user.sendLocale("money.balance.user.add", amount);
                user.sendLocale("money.balance.user.new", user.getPlayerCash());

                amount *= 2f / 3f;

                user.getTeam().addTeamCash(amount);
                user.getTeam().messageLocale("money.balance.team.add", amount);
                user.getTeam().messageLocale("money.balance.team.new", user.getTeam().getTeamCash());

                event.getClickedBlock().setType(Material.AIR);
                break;

        }

    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        User user = plugin.getUser(event.getPlayer());

        if (!user.isInGame()) {
            event.setCancelled(true);
            return;
        }

        resetDurability(event.getPlayer());

        PlayerClassHandler classHandler = user.getPlayerClassHandler();
        if (classHandler.onInteract(new UserRightClickEntityEvent(user, event))) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.getGameState() != GameState.SHOWDOWN) return;
        if (!plugin.getUser(event.getPlayer()).isInGame()) return;

        User user = plugin.getUser(event.getPlayer());
        if (user == null) return;

        //Prevent players from escaping showdown bounds
        if (plugin.getShowdownArena().checkUserMove(user, event.getTo())) {
            if ((user.getTrappedTicks() % 50) == 0) user.sendLocale("showdown.escape");
            user.setTrappedTicks(user.getTrappedTicks() + 1);

            if (user.getTrappedTicks() >= 199) {
                user.teleport(plugin.getShowdownArena().getCenter().clone().add(0, 1, 0));
                user.setTrappedTicks(0);
                plugin.messageAllLocale("showdown.tele-center", user.getFormattedName());
            }
        } else user.setTrappedTicks(0);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        resetDurability((Player) event.getEntity());
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        TeamColor damagerTeam = EntityUtils.getTeamFromEntity(plugin, event.getDamager());
        TeamColor targetTeam = EntityUtils.getTeamFromEntity(plugin, event.getEntity());

        User damagerInfo = EntityUtils.getUserFromEntity(plugin, event.getDamager());

        if (damagerInfo == null) {
            if (damagerTeam == targetTeam) {
                event.setCancelled(true);
            } else plugin.onUserAttacked();
            return;
        }

        if (damagerTeam == null || targetTeam == null) {
            event.setCancelled(true);
            return;
        }

        resetDurability(damagerInfo.getPlayer());

        User targetInfo = null;
        if (event.getEntity() instanceof Player) targetInfo = plugin.getUser((Player) event.getEntity());

        if (damagerTeam == targetTeam) {
            if (!(event.getDamager() instanceof TNTPrimed) || damagerInfo != targetInfo) {
                event.setCancelled(true);
                return;
            }
        }

        userAttack(new UserAttackEvent(damagerInfo, targetInfo, event));
        if(event.isCancelled()) return;

        if (targetInfo == null) return;

        userAttacked(new UserAttackedEvent(targetInfo, damagerInfo, event));
        if(event.isCancelled()) return;

        plugin.onUserAttacked();

        if (targetInfo.getPlayer().getHealth() - event.getFinalDamage() < 1) {
            event.setCancelled(true);
            playerDeath(targetInfo, damagerInfo, event.getCause(), true);
        }
    }

    private void userAttacked(UserAttackedEvent event) {
        PlayerClassHandler classHandler = event.getUser().getPlayerClassHandler();
        if (!event.isCancelled()) classHandler.onUserAttacked(event);
    }

    private void userAttack(UserAttackEvent event) {
        ItemStack weapon = event.getItem();

        if (event.getDamageCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getTargetUser() != null &&
                weapon != null && weapon.containsEnchantment(Enchantment.FIRE_ASPECT)) {
            event.getTargetUser().setFireAttacker(event.getUser());
        }

        PlayerClassHandler classHandler = event.getUser().getPlayerClassHandler();
        classHandler.onInteract(event);
        if (!event.isCancelled()) classHandler.onUserAttack(event);
    }

    public void playerDeath(User died, User killer, EntityDamageEvent.DamageCause cause, boolean intentionally) {
        if (plugin.getGameState() == GameState.AFTERMATH) return;

        if (!died.isInGame()) {
            died.resetPlayerStats(true);

            died.teleport(plugin.getMapSpawn(null));
            return;
        }

        plugin.getGameInstance().onUserDeath();

        removeEntityTargets(died);
        Disguises.unDisguise(died);

        if (killer != null && killer != died) killer.getStatsHolder().addKill();

        displayDeathMessage(died, killer, cause, intentionally);

        died.getStatsHolder().addDeath();

        Team diedTeam = died.getTeam();
        boolean respawn = plugin.getGameState() != GameState.SHOWDOWN &&
                plugin.getRandom().nextFloat() < (diedTeam.getRespawnChance() / 100f);

        if (respawn) {
            plugin.messageAllLocale("game.player.respawn", died.getFormattedName());

            diedTeam.setRespawnChance(diedTeam.getRespawnChance() - 15);
            diedTeam.respawnPlayer(died);

            died.resetPlayerStats(false);
        } else {
            plugin.messageAllLocale("game.player.no-respawn", died.getFormattedName());
            died.removeFromGame();
            died.setSpectator();
        }
    }

    private void removeEntityTargets(User user) {
        for (Entity e : user.getPlayer().getWorld().getEntities()) {
            if (!(e instanceof Creature)) continue;

            Creature creature = (Creature) e;
            if (creature.getTarget() == null || creature.getTarget() != user.getPlayer()) continue;
            creature.setTarget(null);
        }
    }

    private void displayDeathMessage(User diedInfo, User killerInfo, EntityDamageEvent.DamageCause cause,
                                     boolean intentionally) {
        Object[] args;
        String locale;
        if (killerInfo != null) {
            if (intentionally) locale = "game.player.killed";
            else locale = "game.player.died-fighting";

            args = new Object[]{diedInfo.getFormattedName(), killerInfo.getFormattedName()};
        } else {
            locale = "game.player.death";
            args = new Object[]{diedInfo.getFormattedName()};
        }

        String ending = DEATH_MESSAGE_ENDINGS.get(cause);
        if (ending != null) locale += ending;

        plugin.messageAllLocale(locale, args);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        User target = plugin.getUser((Player) event.getEntity());

        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION)
            new FixExecutor().onCommand(target, null, "fix", new String[0]);

        if (!target.isInGame()) {
            //Prevent spectators from being damaged
            event.setCancelled(true);
            return;
        }

        if (target.getPlayer().getHealth() - event.getFinalDamage() < 1) {
            event.setCancelled(true);

            User killer = null;

            switch (event.getCause()) {
                case FIRE_TICK:
                    killer = target.getFireAttacker();
                    break;
                case WITHER:
                    killer = target.getWitherAttacker();
                    break;
            }

            boolean intentionally = true;

            if (killer == null) {
                intentionally = false;
                killer = target.getLastAttacker();
            }

            playerDeath(target, killer, event.getCause(), intentionally);
        } else if (target.isCloaked()) {
            target.getPlayer().getWorld().playSound(target.getPlayer().getLocation(), Sound.HURT_FLESH, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ProjectileSource projectileSource = event.getEntity().getShooter();

        if (!(projectileSource instanceof Player)) return;
        Player shooter = (Player) projectileSource;
        User shooterInfo = plugin.getUser(shooter);

        if (!shooterInfo.isInGame() || shooterInfo.getTeamColor() == null) {
            event.setCancelled(true);
            return;
        }

        PotionEffectType type = event.getPotion().getEffects().iterator().next().getType();

        //If NPE then add potion to GOOD_POTIONS
        boolean good = GOOD_POTIONS.get(type);

        for (LivingEntity ent : event.getAffectedEntities()) {
            if (!(ent instanceof Player)) continue;

            Player player = (Player) ent;
            User user = plugin.getUser(player);

            if (!user.isInGame()) {
                event.setIntensity(ent, 0);
                continue;
            }

            if ((user.getTeamColor() == shooterInfo.getTeamColor()) != good) {
                event.setIntensity(ent, 0);
            } else if (type == PotionEffectType.HEAL) {
                event.setIntensity(ent, event.getIntensity(ent) * user.getPotionStrengthModifier());
                user.setPotionStrengthModifier(Math.max(user.getPotionStrengthModifier() - 0.05d, 0.5d));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        User diedInfo = plugin.getUser(event.getPlayer());
        Team diedTeam = diedInfo.getTeam();
        if (diedTeam == null) return;

        diedInfo.removeFromGame();
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        User user = plugin.getUser((Player) event.getEntity());
        if (user == null || user.isInGame()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType().equals(InventoryType.SlotType.ARMOR)) event.setCancelled(true);

        User user = plugin.getUser((Player) event.getWhoClicked());
        if (!user.isInGame() || user.getTeamColor() == null) {
            plugin.handleSpectatorInventory(event);
            return;
        }

        if (event.getInventory().getType() != InventoryType.PLAYER &&
                event.getInventory().getType() != InventoryType.CRAFTING) {

            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            Team team = user.getTeam();

            PlayerClassHandler classHandler = user.getPlayerClassHandler();

            Building building = plugin.getBuildingInfo(user.getShopBlock());
            if (building == null) return;

            boolean done = classHandler.onInventoryClick(event.getCurrentItem(), building, user, team);

            if (done) return;

            InventoryHandler handler = plugin.getBuildingInventoryHandler();
            if (handler == null) return;

            handler.onInventoryClick(event.getCurrentItem(), building, user, team);
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        User user = plugin.getUser(player);

        if (user.getShopInventory() == null || user.getShopInventory().getTitle() == null) return;

        if (user.getShopInventory().getTitle().equals(event.getInventory().getTitle())) {
            user.shopInventoryClosed();
        }
    }


}
