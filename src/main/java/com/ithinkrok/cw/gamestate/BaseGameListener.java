package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.event.ShopOpenEvent;
import com.ithinkrok.cw.metadata.*;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.map.*;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.api.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.api.event.user.world.*;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.util.TreeFeller;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.minigames.api.schematic.Facing;
import com.ithinkrok.minigames.api.task.TaskScheduler;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.*;
import com.ithinkrok.minigames.api.util.math.Calculator;
import com.ithinkrok.minigames.api.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.api.util.math.SingleValueVariables;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by paul on 16/01/16.
 */
public class BaseGameListener extends BaseGameStateListener {

    private static final Map<PotionEffectType, Boolean> GOOD_POTIONS = new HashMap<>();

    static {
        GOOD_POTIONS.put(PotionEffectType.HEAL, true);
        GOOD_POTIONS.put(PotionEffectType.INCREASE_DAMAGE, true);
        GOOD_POTIONS.put(PotionEffectType.SPEED, true);
        GOOD_POTIONS.put(PotionEffectType.FAST_DIGGING, true);
        GOOD_POTIONS.put(PotionEffectType.INVISIBILITY, true);
        GOOD_POTIONS.put(PotionEffectType.ABSORPTION, true);
        GOOD_POTIONS.put(PotionEffectType.SATURATION, true);
        GOOD_POTIONS.put(PotionEffectType.JUMP, true);
        GOOD_POTIONS.put(PotionEffectType.WATER_BREATHING, true);
        GOOD_POTIONS.put(PotionEffectType.FIRE_RESISTANCE, true);
        GOOD_POTIONS.put(PotionEffectType.DAMAGE_RESISTANCE, true);

        GOOD_POTIONS.put(PotionEffectType.HARM, false);
        GOOD_POTIONS.put(PotionEffectType.CONFUSION, false);
        GOOD_POTIONS.put(PotionEffectType.BLINDNESS, false);
        GOOD_POTIONS.put(PotionEffectType.POISON, false);
        GOOD_POTIONS.put(PotionEffectType.SLOW, false);
        GOOD_POTIONS.put(PotionEffectType.SLOW_DIGGING, false);
        GOOD_POTIONS.put(PotionEffectType.WEAKNESS, false);
        GOOD_POTIONS.put(PotionEffectType.WITHER, false);

    }

    private final WeakHashMap<Config, GoldConfig> goldConfigMap = new WeakHashMap<>();

    protected String showdownGameState;
    private String goldSharedConfig;
    private String unknownBuildingLocale;
    private String cannotBuildHereLocale;
    private String buildingNotFinishedLocale;
    private String buildingNotYoursLocale;
    private String cannotDestroyOwnBuildingLocale;
    private String cannotDestroyLocale;
    private String buildingDestroyedLocale;
    private String respawnLocale;
    private String noRespawnLocale;
    private int lostRespawnChance;
    private String teamLostPlayerLocale;
    private String teamPlayersLeftLocale;
    private String aftermathGameState;
    private String teamWinLocale;

    private int showdownStartTeams;
    private int showdownStartPlayers;

    private CountdownConfig showdownCountdown;

    private String deathKillAndAssistLocale;
    private String deathKillLocale;
    private String deathAssistLocale;
    private String deathNaturalLocale;

    private String spectatorJoinLocale, spectatorQuitLocale;
    private String inGameJoinLocale, inGameQuitLocale;

    private int buildingDestroyWait;

    private Calculator enderAmount;
    private String enderFoundLocale;

    private String motdGameLocale;
    private String motdShowdownLocale;
    private String motdAftermathLocale;

    private ParticleEffect bloodEffect;

    @CustomEventHandler
    public void onUserChat(UserChatEvent event) {
        if (event.getUser().isInGame()) {
            String kitName = event.getUser().getKitName().toUpperCase();
            String teamColor = event.getUser().getTeamIdentifier().getChatColor().toString();

            event.setFormat(
                    teamColor + "<" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + kitName + ChatColor.DARK_GRAY +
                            "] %s" + teamColor + "> " + ChatColor.WHITE + "%s");
        } else {
            event.setFormat(ChatColor.LIGHT_PURPLE + "<" + ChatColor.GRAY + "%s" + ChatColor.LIGHT_PURPLE + "> " +
                    ChatColor.WHITE + "%s");
        }
    }

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);

        Config config = event.getConfig();

        goldSharedConfig = config.getString("gold_shared_object");

        unknownBuildingLocale = config.getString("buildings.unknown_locale", "building.unknown");
        cannotBuildHereLocale = config.getString("buildings.invalid_location_locale", "building.invalid_loc");
        buildingNotFinishedLocale = config.getString("buildings.not_finished_locale", "building.not_finished");
        buildingNotYoursLocale = config.getString("buildings.not_yours_locale", "building.not_yours");

        cannotDestroyOwnBuildingLocale =
                config.getString("buildings.destroy_own_team_locale", "building.destroy.own_team");
        cannotDestroyLocale = config.getString("buildings.destroy_protected_locale", "building.destroy.protected");
        buildingDestroyedLocale = config.getString("buildings.destroyed_locale", "building.destroy.success");

        respawnLocale = config.getString("respawn_locale", "respawn.success");
        noRespawnLocale = config.getString("no_respawn_locale", "respawn.fail");

        teamLostPlayerLocale = config.getString("team_lost_player_locale", "team.lost_player");
        teamPlayersLeftLocale = config.getString("team_players_left_locale", "team.players_left");
        teamWinLocale = config.getString("team_win_locale", "team.win");

        aftermathGameState = config.getString("aftermath_gamestate", "aftermath");

        showdownCountdown =
                MinigamesConfigs.getCountdown(config, "showdown_countdown", "showdown", 30, "countdowns.showdown");

        showdownStartTeams = config.getInt("showdown_start.teams");
        showdownStartPlayers = config.getInt("showdown_start.players");

        buildingDestroyWait = (int) (config.getDouble("buildings.destroy_wait", 3.0d) * 20d);
        lostRespawnChance = config.getInt("lost_respawn_chance", 15);

        showdownGameState = config.getString("showdown_gamestate", "showdown");

        deathAssistLocale = config.getString("death_assist_locale", "death.assist");
        deathKillAndAssistLocale = config.getString("death_kill_and_assist_locale", "death.kill_and_assist");
        deathKillLocale = config.getString("death_kill_locale", "death.kill");
        deathNaturalLocale = config.getString("death_natural_locale", "death.natural");

        inGameJoinLocale = config.getString("ingame_user_join_locale", "user.join.game");
        inGameQuitLocale = config.getString("ingame_user_quit_locale", "user.quit.game");

        spectatorJoinLocale = config.getString("spectator_join_locale", "user.join.spectator");
        spectatorQuitLocale = config.getString("spectator_quit_locale", "user.quit.spectator");

        enderAmount = new ExpressionCalculator(config.getString("ender_amount"));
        enderFoundLocale = config.getString("ender_found_locale", "ender_chest.found");

        motdGameLocale = config.getString("motd.in_game_locale", "motd.cw_game");
        motdShowdownLocale = config.getString("motd.showdown_locale", "motd.showdown");
        motdAftermathLocale = config.getString("motd.aftermath_locale", "motd.aftermath");

        bloodEffect = MinigamesConfigs.getParticleEffect(config, "blood_effect");
    }

    @CustomEventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        Config goldShared = event.getGameGroup().getSharedObject(goldSharedConfig);
        GoldConfig gold = getGoldConfig(goldShared);

        if (!gold.allowItemPickup(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
        }
    }

    @CustomEventHandler
    public void sendQuitMessageOnUserQuit(UserQuitEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount();
        int maxPlayers = Bukkit.getMaxPlayers();

        if (event.getUser().isInGame()) {
            event.getUserGameGroup().sendLocale(inGameQuitLocale, name, currentPlayers, maxPlayers);
        } else {
            if(!event.getUser().isPlayer()) return;
            event.getUserGameGroup().sendLocale(spectatorQuitLocale, name, currentPlayers, maxPlayers);
        }
    }


    @CustomEventHandler
    public void sendJoinMessageOnUserJoin(UserJoinEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount();
        int maxPlayers = event.getUserGameGroup().getMaxPlayers();

        if (event.getUser().isInGame()) {
            event.getUserGameGroup().sendLocale(inGameJoinLocale, name, currentPlayers, maxPlayers);
        } else {
            event.getUserGameGroup().sendLocale(spectatorJoinLocale, name, currentPlayers, maxPlayers);
        }
    }

    private GoldConfig getGoldConfig(Config config) {
        GoldConfig gold = goldConfigMap.get(config);

        if (gold == null) {
            gold = new GoldConfig(config);
            goldConfigMap.put(config, gold);
        }

        return gold;
    }

    @CustomEventHandler
    public void onGameStateChange(GameStateChangedEvent event) {
        event.getGameGroup().doInFuture(task -> {
            updateMotd(event.getGameGroup());
        });
    }

    private void updateMotd(GameGroup gameGroup) {
        switch (gameGroup.getCurrentGameState().getName()) {
            case "game":
                gameGroup.setMotd(gameGroup.getLocale(motdGameLocale, getNonZombieUsersInGame(gameGroup)));
                return;
            case "showdown":
                gameGroup.setMotd(gameGroup.getLocale(motdShowdownLocale));
                return;
            case "aftermath":
                gameGroup.setMotd(gameGroup.getLocale(motdAftermathLocale));
        }
    }

    public int getNonZombieUsersInGame(GameGroup gameGroup) {
        int count = 0;

        for (User user : gameGroup.getUsers()) {
            if (!user.isInGame() || !user.isPlayer()) continue;
            ++count;
        }

        return count;
    }

    @CustomEventHandler
    public void onUserFoodLevelChange(UserFoodLevelChangeEvent event) {
        if (!event.getUser().isInGame()) event.setCancelled(true);
    }

    @CustomEventHandler
    public void onUserInteractWorld(UserInteractWorldEvent event) {
        if (!event.getUser().isInGame()) {
            event.setCancelled(true);
            return;
        }


        if (event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK || !event.hasBlock()) return;

        switch (event.getClickedBlock().getType()) {
            case OBSIDIAN:
                event.setCancelled(true);
                BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

                Building building = controller.getBuilding(event.getClickedBlock().getLocation());
                if (building == null) return;

                if (!building.getTeamIdentifier().equals(event.getUser().getTeamIdentifier())) {
                    event.getUser().showAboveHotbarLocale(buildingNotYoursLocale);
                    return;
                }

                if (!building.isFinished()) {
                    event.getUser().showAboveHotbarLocale(buildingNotFinishedLocale);
                    return;
                }

                ClickableInventory shop = building.createShop();
                if (shop == null) shop = new ClickableInventory(building.getBuildingName());

                event.getUserGameGroup().userEvent(new ShopOpenEvent(event.getUser(), building, shop));

                event.getUser().showInventory(shop, event.getClickedBlock().getLocation());
                return;
            case ENDER_CHEST:
                event.setCancelled(true);

                event.getUser().getTeam().sendLocale(enderFoundLocale, event.getUser().getFormattedName());

                int amount = (int) enderAmount.calculate(event.getUser().getUpgradeLevels());

                Money.getOrCreate(event.getUser()).addMoney(amount, true);
                Money.getOrCreate(event.getUser().getTeam()).addMoney((int) (amount * 2f / 3f), true);

                event.getClickedBlock().setType(Material.AIR);
        }


    }

    @CustomEventHandler
    public void onUserDeath(UserDeathEvent event) {
        User died = event.getUser();

        if (!died.isInGame()) {
            died.resetUserStats(true);

            died.teleport(died.getGameGroup().getCurrentMap().getSpawn());
            return;
        }

        //TODO remove entity targets on the dead player

        event.getUser().unDisguise();

        displayDeathMessage(event);

        User killer = event.getKillerUser();
        if (killer == null) killer = event.getAssistUser();

        if (killer != null) {
            StatsHolder killerStats = StatsHolder.getOrCreate(killer);
            killerStats.addKill();
        }

        StatsHolder deathStats = StatsHolder.getOrCreate(died);
        deathStats.addDeath();

        Team team = died.getTeam();
        CWTeamStats teamStats = CWTeamStats.getOrCreate(team);

        boolean respawn = shouldRespawnUser(died, teamStats);

        if (respawn) {
            died.getGameGroup().sendLocale(respawnLocale, died.getFormattedName());

            teamStats.setRespawnChance(teamStats.getRespawnChance() - lostRespawnChance, true);

            teamStats.respawnUser(died);
            died.resetUserStats(false);
        } else {
            died.getGameGroup().sendLocale(noRespawnLocale, died.getFormattedName());

            removeUserFromGame(died);

            makeUserSpectator(died);
        }
    }

    private void displayDeathMessage(UserDeathEvent event) {
        String localeEnding = "." + event.getKillCause().toString().toLowerCase();

        if (event.hasKillerUser()) {
            if (event.hasAssistUser()) {
                sendDeathMessage(event.getUserGameGroup(), deathKillAndAssistLocale, localeEnding,
                        event.getUser().getFormattedName(), event.getKillerUser().getFormattedName(),
                        event.getAssistUser().getFormattedName());

            } else {
                sendDeathMessage(event.getUserGameGroup(), deathKillLocale, localeEnding,
                        event.getUser().getFormattedName(), event.getKillerUser().getFormattedName());
            }
        } else if (event.hasAssistUser()) {
            sendDeathMessage(event.getUserGameGroup(), deathAssistLocale, localeEnding,
                    event.getUser().getFormattedName(), event.getAssistUser().getFormattedName());
        } else {
            sendDeathMessage(event.getUserGameGroup(), deathNaturalLocale, localeEnding,
                    event.getUser().getFormattedName());
        }
    }

    public boolean shouldRespawnUser(User user, CWTeamStats teamStats) {
        return user.isPlayer() && random.nextFloat() < (teamStats.getRespawnChance() / 100f);
    }

    private void removeUserFromGame(User died) {
        Team team = died.getTeam();

        died.setInGame(false);

        died.getGameGroup().sendLocale(teamLostPlayerLocale, team.getFormattedName());
        died.getGameGroup().sendLocale(teamPlayersLeftLocale, team.getUserCount(), team.getFormattedName());

        died.setDisplayName(died.getName());
        died.setTabListName(died.getName());

        died.setScoreboardHandler(null);

        if (team.getUserCount() == 0) {
            CWTeamStats.getOrCreate(team).eliminate();
        }

        checkVictory(died.getGameGroup(), true);

        if (!died.isPlayer()) died.removeNonPlayer();

        updateMotd(died.getGameGroup());
    }

    private void sendDeathMessage(GameGroup gameGroup, String locale, String ending, Object... args) {
        String message = gameGroup.getLocale(locale + ending, args);
        if (message == null) message = gameGroup.getLocale(locale, args);

        gameGroup.sendMessage(message);
    }

    @CustomEventHandler
    public void onUserDamaged(UserDamagedEvent event) {
        if (!event.getUser().isInGame()) {
            event.setCancelled(true);
            return;
        }

        if (event.getFinalDamage() < 0.5) return;

        playBloodEffect(event.getUser(), event.getUser().getEntity());
    }

    private void playBloodEffect(TaskScheduler taskScheduler, Entity entity) {
        if (bloodEffect == null) return;

        taskScheduler.repeatInFuture(task -> {
            bloodEffect.playEffect(entity.getLocation().clone().add(0, 0.8, 0));

            if (task.getRunCount() > 3) task.finish();
        }, 1, 3);

    }

    @CustomEventHandler(ignoreCancelled = true)
    public void onEntityDamaged(MapEntityDamagedEvent event) {
        if (event.getFinalDamage() < 0.5) return;

        playBloodEffect(event.getGameGroup(), event.getEntity());
    }

    @CustomEventHandler
    public void onUserQuit(UserQuitEvent event) {
        if (event.getReason() != UserQuitEvent.QuitReason.QUIT_SERVER) return;

        if (event.getUser().isInGame()) {
            event.getUser().becomeEntity(EntityType.ZOMBIE);
            checkVictory(event.getUser().getGameGroup(), true);
            event.setRemoveUser(false);
        }
    }

    public void checkVictory(GameGroup gameGroup, boolean checkShowdown) {
        int nonZombieUsersInGame = 0;

        for (User user : gameGroup.getUsers()) {
            if (user.isInGame() && user.isPlayer()) ++nonZombieUsersInGame;
        }

        if (nonZombieUsersInGame == 0) {
            gameGroup.changeGameState(aftermathGameState);
            return;
        }

        Set<Team> teamsInGame = new HashSet<>();

        for (User user : gameGroup.getUsers()) {
            if (!user.isInGame()) continue;

            teamsInGame.add(user.getTeam());
        }

        if (teamsInGame.size() > 1) {
            if (!checkShowdown) return;

            checkShowdownStart(gameGroup, teamsInGame.size(), nonZombieUsersInGame);
            return;
        }

        Team winner = teamsInGame.iterator().next();
        gameGroup.sendLocale(teamWinLocale, winner.getFormattedName());

        TeamStatsHolderGroup winnerStats = TeamStatsHolderGroup.getOrCreate(winner);
        winnerStats.addGameWin();

        gameGroup.changeGameState(aftermathGameState);

        updateMotd(gameGroup);
    }

    protected void checkShowdownStart(GameGroup gameGroup, int teamsInGame, int nonZombieUsersInGame) {
        if (gameGroup.hasActiveCountdown(showdownCountdown.getName())) return;

        boolean teamCheck = teamsInGame > showdownStartTeams || gameGroup.getTeamIdentifiers().size() < 3;
        boolean playerCheck = nonZombieUsersInGame > showdownStartPlayers;

        if (teamCheck && playerCheck) return;

        gameGroup.startCountdown(showdownCountdown);
    }

    @CustomEventHandler
    public void onBlockBurn(MapBlockBurnEvent event) {
        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onBlockSpread(MapBlockGrowEvent event) {
        if (!event.isSpreadEvent()) return;
        if (event.getNewState().getType() != Material.FIRE) return;

        event.getSpreadSource().setType(Material.AIR);

        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onPotionSplash(MapPotionSplashEvent event) {
        if (!event.hasThrowerUser()) return;

        PotionEffectType type = event.getPotion().getEffects().iterator().next().getType();
        boolean good = GOOD_POTIONS.get(type);

        for (LivingEntity entity : event.getAffected()) {
            User rep = EntityUtils.getRepresentingUser(event.getThrowerUser(), entity);

            if (rep == null) continue;

            if (Objects.equals(event.getThrowerUser().getTeamIdentifier(), rep.getTeamIdentifier()) != good) {
                event.setIntensity(entity, 0);
            } else if (type == PotionEffectType.HEAL) {
                PotionStrengthModifier psm = PotionStrengthModifier.getOrCreate(rep);

                event.setIntensity(entity, event.getIntensity(entity) * psm.getPotionStrengthModifier());
                psm.onPotionUsed();
            }
        }
    }

    @CustomEventHandler
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        if (!event.getUser().isInGame()) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() != Material.OBSIDIAN) {
            Config goldShared = event.getUserGameGroup().getSharedObject(goldSharedConfig);
            GoldConfig gold = getGoldConfig(goldShared);

            gold.onBlockBreak(event.getBlock(), event.getUserGameGroup());
            return;
        }

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());
        Building building = controller.getBuilding(event.getBlock().getLocation());

        if (building == null) return;

        if (building.getTeamIdentifier().equals(event.getUser().getTeamIdentifier())) {
            event.getUser().sendLocale(cannotDestroyOwnBuildingLocale);
            event.setCancelled(true);
        } else if (building.isProtected()) {
            event.getUser().sendLocale(cannotDestroyLocale, building.getBuildingName());
            event.setCancelled(true);
        } else {
            event.getUserGameGroup()
                    .sendLocale(buildingDestroyedLocale, event.getUser().getFormattedName(), building.getBuildingName(),
                            building.getTeamIdentifier().getFormattedName());
            event.getUserGameGroup().doInFuture(task -> building.explode(), buildingDestroyWait);
        }
    }

    @CustomEventHandler
    public void onUserPickupItem(UserPickupItemEvent event) {
        if (!event.getUser().isInGame()) {
            event.setCancelled(true);
            return;
        }

        GoldConfig goldConfig = getGoldConfig(event.getUserGameGroup().getSharedObject(goldSharedConfig));
        Material material = event.getItem().getItemStack().getType();

        event.setCancelled(true);

        if (goldConfig.allowItemPickup(material)) {
            int amount = event.getItem().getItemStack().getAmount();

            int userGold = goldConfig.getUserGold(material) * amount;
            Money userMoney = Money.getOrCreate(event.getUser());
            userMoney.addMoney(userGold, false);

            int teamGold = goldConfig.getTeamGold(material) * amount;
            Money teamMoney = Money.getOrCreate(event.getUser().getTeam());
            teamMoney.addMoney(teamGold, false);

            SoundEffect sound = new SoundEffect(goldConfig.getPickupSound(), 1.0f, 0.8f + (random.nextFloat()) * 0.4f);
            event.getUser().playSound(event.getUser().getLocation(), sound);
        }

        event.getItem().remove();
    }

    @CustomEventHandler
    public void onUserPlaceBlock(UserPlaceBlockEvent event) {
        if (!event.getUser().isInGame()) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() != Material.LAPIS_ORE) return;

        event.getBlock().setType(Material.AIR);

        String buildingType = InventoryUtils.getItemName(event.getItemPlaced());

        boolean instaBuild = InventoryUtils.loreContainsLine(event.getItemPlaced(), "Instabuild");

        if (buildingType == null || event.getUserGameGroup().getSchematic(buildingType) == null) {
            event.getUser().sendLocale(unknownBuildingLocale);
            return;
        }

        int rotation = Facing.getFacing(event.getUser().getLocation().getYaw());

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

        if (!controller.buildBuilding(buildingType, event.getUser().getTeamIdentifier(), event.getBlock().getLocation(),
                rotation, instaBuild)) {
            event.getUser().sendLocale(cannotBuildHereLocale);
            event.setCancelled(true);
        }
    }

    @CustomEventHandler
    public void onBlockBreakNaturally(MapBlockBreakNaturallyEvent event) {
        Config goldShared = event.getGameGroup().getSharedObject(goldSharedConfig);
        GoldConfig gold = getGoldConfig(goldShared);

        gold.onBlockBreak(event.getBlock(), event.getGameGroup());
    }

    @CustomEventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if (!event.getCountdown().getName().equals(showdownCountdown.getName())) return;

        event.getGameGroup().changeGameState(showdownGameState);

        updateMotd(event.getGameGroup());
    }

    protected static class GoldConfig {
        Map<Material, ItemStack> oreBlocks = new EnumMap<>(Material.class);

        Map<Material, Integer> userGold = new EnumMap<>(Material.class);
        Map<Material, Integer> teamGold = new EnumMap<>(Material.class);

        boolean treesEnabled;
        Material treeItemMaterial;
        ExpressionCalculator treeItemAmount;
        Set<Material> logMaterials = new HashSet<>();

        Sound pickupSound;

        public GoldConfig(Config config) {
            Config ores = config.getConfigOrNull("ore_blocks");
            if (ores != null) {
                for (String matName : ores.getKeys(false)) {
                    Material material = Material.matchMaterial(matName);
                    ItemStack item = MinigamesConfigs.getItemStack(ores, matName);
                    oreBlocks.put(material, item);
                }
            }

            Config items = config.getConfigOrNull("items");
            for (String matName : items.getKeys(false)) {
                Material material = Material.matchMaterial(matName);
                Config matConfig = items.getConfigOrNull(matName);

                userGold.put(material, matConfig.getInt("user"));
                teamGold.put(material, matConfig.getInt("team"));
            }

            pickupSound = Sound.valueOf(config.getString("pickup_sound").toUpperCase());

            Config trees = config.getConfigOrNull("trees");
            treesEnabled = trees != null && trees.getBoolean("enabled");

            if (!treesEnabled) return;

            treeItemMaterial = Material.matchMaterial(trees.getString("item_material"));

            treeItemAmount = new ExpressionCalculator(trees.getString("item_amount"));

            List<String> logMaterialNames = trees.getStringList("log_materials");
            logMaterials.addAll(logMaterialNames.stream().map(Material::matchMaterial).collect(Collectors.toList()));
        }

        public void onBlockBreak(Block block, GameGroup gameGroup) {
            ItemStack drop = null;
            if (oreBlocks.containsKey(block.getType())) {
                drop = oreBlocks.get(block.getType()).clone();
            } else if (treesEnabled && logMaterials.contains(block.getType())) {
                int count = TreeFeller.fellTree(block.getLocation(), BuildingController.getOrCreate(gameGroup));
                count = (int) treeItemAmount.calculate(new SingleValueVariables(count));
                if (count > 0) drop = new ItemStack(treeItemMaterial, count);
            }

            block.setType(Material.AIR);

            if (drop == null) return;
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5d, 0.1d, 0.5d), drop);
        }

        public Sound getPickupSound() {
            return pickupSound;
        }

        public boolean allowItemPickup(Material material) {
            return userGold.containsKey(material);
        }

        public int getUserGold(Material material) {
            Integer result = userGold.get(material);
            return result == null ? 0 : result;
        }

        public int getTeamGold(Material material) {
            Integer result = teamGold.get(material);
            return result == null ? 0 : result;
        }
    }
}
