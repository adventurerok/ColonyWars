package com.ithinkrok.mccw.lobby;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.*;
import com.ithinkrok.mccw.util.BoundingBox;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by paul on 29/12/15.
 */
public class SpleefMinigame extends LobbyMinigameAdapter {

    private static final Material SPADE = Material.STONE_SPADE;

    private final WarsPlugin plugin;

    private List<Vector> queueButtons;
    private List<Vector> spawnLocations;
    private Vector exitLocation;
    private BoundingBox snowBounds;

    private List<UUID> usersInSpleef = new ArrayList<>();
    private LinkedHashSet<UUID> queue = new LinkedHashSet<>();
    private int extraRadius;

    public SpleefMinigame(WarsPlugin plugin) {
        this.plugin = plugin;

        queueButtons = plugin.getWarsConfig().getSpleefQueueButtonLocations();
        spawnLocations = plugin.getWarsConfig().getSpleefSpawnLocations();
        exitLocation = plugin.getWarsConfig().getSpleefExitLocation();
        snowBounds = plugin.getWarsConfig().getSpleefSnowBounds();
        extraRadius = plugin.getWarsConfig().getSpleefExtraRadius();
    }

    @Override
    public void resetMinigame() {
        resetArena();

        usersInSpleef.clear();
        queue.clear();
    }

    private void resetArena() {
        World world = plugin.getServer().getWorld(plugin.getLobbyWorldName());

        for(int x = snowBounds.min.getBlockX(); x <= snowBounds.max.getBlockX(); ++x) {
            for(int y = snowBounds.min.getBlockY(); y <= snowBounds.max.getBlockY(); ++y) {
                for(int z = snowBounds.min.getBlockZ(); z <= snowBounds.max.getBlockZ(); ++z) {
                    world.getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    @Override
    public void onUserMove(UserMoveEvent event) {
        if(!usersInSpleef.contains(event.getUser().getUniqueId())) return;

        double x = event.getUser().getLocation().getX();
        double z = event.getUser().getLocation().getZ();

        if(x + extraRadius < snowBounds.min.getX() || x - extraRadius > snowBounds.max.getX() || z + extraRadius <
                snowBounds.min.getZ() || z - extraRadius > snowBounds.max.getZ()) {
            removeUserFromSpleef(event.getUser());
        }
    }

    @Override
    public void onUserDamaged(UserDamagedEvent event) {
        if(event.getDamageCause() != EntityDamageEvent.DamageCause.LAVA) return;

        spleefUserKilled(event.getUser());
    }

    @Override
    public void onUserQuitLobby(UserQuitLobbyEvent event) {
        queue.remove(event.getUser().getUniqueId());
        spleefUserKilled(event.getUser());
    }

    private void spleefUserKilled(User user) {
        if(!usersInSpleef.remove(user.getUniqueId())) return;

        removeUserFromSpleef(user);
        plugin.messageAllLocale("minigames.spleef.loser", user.getFormattedName());

        if(usersInSpleef.size() == 1) {
            User winner = plugin.getUser(usersInSpleef.remove(0));

            removeUserFromSpleef(winner);
            plugin.messageAllLocale("minigames.spleef.winner", winner.getFormattedName());

            tryStartGame();
        }
    }

    private void removeUserFromSpleef(User user) {
        user.teleport(getExitLocation());

        user.getPlayerInventory().remove(SPADE);

        user.setGameMode(GameMode.ADVENTURE);
    }

    private Location getExitLocation() {
        World world = plugin.getServer().getWorld(plugin.getLobbyWorldName());

        return new Location(world, exitLocation.getX(), exitLocation.getY(), exitLocation.getZ());
    }

    @Override
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        if (event.getBlock().getType() == Material.SNOW_BLOCK) event.setCancelled(false);
    }

    @Override
    public boolean onUserInteract(UserInteractEvent event) {
        if(!event.hasBlock()) return false;

        switch(event.getClickedBlock().getType()) {
            case SNOW_BLOCK:
                if(!event.isRightClick()) event.setCancelled(false);
                return true;
            case STONE_BUTTON:
                if(!event.isRightClick()) return false;
                if(!queueButtons.contains(event.getClickedBlock().getLocation().toVector())) return false;
                addUserToQueue(event.getUser());
                return true;
        }

        return false;
    }

    private void addUserToQueue(User user) {
        boolean success = queue.add(user.getUniqueId());
        if(!success) {
            user.sendLocale("minigames.spleef.queue.already-joined");
            return;
        }

        user.sendLocale("minigames.spleef.queue.join");

        tryStartGame();
    }

    private void tryStartGame() {
        if(!usersInSpleef.isEmpty() || queue.size() < spawnLocations.size()) return;

        resetArena();

        World world = plugin.getServer().getWorld(plugin.getLobbyWorldName());

        Iterator<UUID> iterator = queue.iterator();
        for (Vector spawn : spawnLocations) {
            UUID joiningUUID = iterator.next();
            iterator.remove();

            User joining = plugin.getUser(joiningUUID);

            Location teleport =
                    new Location(world, spawn.getX(), spawn.getY(), spawn.getZ(), joining.getLocation().getYaw(), 0);

            joining.teleport(teleport);
            joining.getPlayerInventory().addItem(new ItemStack(SPADE));
            joining.setGameMode(GameMode.SURVIVAL);
            usersInSpleef.add(joining.getUniqueId());
        }

        plugin.messageAllLocale("minigames.spleef.begin");
    }
}
