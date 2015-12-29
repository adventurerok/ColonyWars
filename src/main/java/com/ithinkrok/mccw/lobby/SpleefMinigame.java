package com.ithinkrok.mccw.lobby;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserBreakBlockEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by paul on 29/12/15.
 */
public class SpleefMinigame extends LobbyMinigameAdapter {

    private final WarsPlugin plugin;

    private List<Vector> queueButtons;
    private List<Vector> spawnLocations;
    private Vector exitLocation;
    private BoundingBox snowBounds;

    private List<UUID> usersInSpleef = new ArrayList<>();
    private LinkedHashSet<UUID> queue = new LinkedHashSet<>();

    public SpleefMinigame(WarsPlugin plugin) {
        this.plugin = plugin;

        queueButtons = plugin.getWarsConfig().getSpleefQueueButtonLocations();
        spawnLocations = plugin.getWarsConfig().getSpleefSpawnLocations();
        exitLocation = plugin.getWarsConfig().getSpleefExitLocation();
        snowBounds = plugin.getWarsConfig().getSpleefSnowBounds();
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

        if(!usersInSpleef.isEmpty() || queue.size() < spawnLocations.size()) return;

        World world = plugin.getServer().getWorld(plugin.getLobbyWorldName());

        Iterator<UUID> iterator = queue.iterator();
        for (Vector spawn : spawnLocations) {
            UUID joiningUUID = iterator.next();
            iterator.remove();

            User joining = plugin.getUser(joiningUUID);

            Location teleport =
                    new Location(world, spawn.getX(), spawn.getY(), spawn.getZ(), joining.getLocation().getYaw(), 0);

            joining.teleport(teleport);
            usersInSpleef.add(joining.getUniqueId());
        }
    }
}
