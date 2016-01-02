package com.ithinkrok.cw.lobbygames;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.*;
import com.ithinkrok.minigames.event.user.*;
import com.ithinkrok.minigames.util.BoundingBox;
import com.ithinkrok.minigames.util.ConfigUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by paul on 01/01/16.
 */
public class SpleefMinigame implements ConfiguredListener {

    private Material spadeMaterial;

    private Map<Vector, Arena> queueButtons = new HashMap<>();
    private Map<UUID, Arena> queueLookups = new HashMap<>();
    private Map<UUID, Arena> gameLookups = new HashMap<>();

    @Override
    public void configure(ConfigurationSection config) {
        spadeMaterial = Material.matchMaterial(config.getString("spade", "IRON_SPADE"));

        ConfigurationSection arenasConfig = config.getConfigurationSection("arenas");
        for(String key : arenasConfig.getKeys(false)){
            ConfigurationSection arenaConfig = arenasConfig.getConfigurationSection(key);

            Arena arena = new Arena(arenaConfig);

            for(Vector button : arena.queueButtons){
                queueButtons.put(button, arena);
            }
        }
    }

    @EventHandler
    public void onUserTeleport(UserTeleportEvent<User> event) {
        Arena arena = gameLookups.get(event.getUser().getUuid());

        if(arena == null) return;

        double x = event.getUser().getLocation().getX();
        double z = event.getUser().getLocation().getZ();

        if(arena.checkUserInBounds(x, z)) return;

        arena.spleefUserKilled(event.getUser(), false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUserBreakBlock(UserBreakBlockEvent<User> event){
        if(event.getBlock().getType() == Material.SNOW_BLOCK) event.setCancelled(false);
    }

    @EventHandler
    public void onUserDamaged(UserDamagedEvent<User> event) {
        if(event.getDamageCause() != EntityDamageEvent.DamageCause.LAVA) return;

        Arena arena = gameLookups.get(event.getUser().getUuid());
        if(arena == null) return;

        event.setCancelled(true);

        arena.spleefUserKilled(event.getUser(), true);
    }

    @EventHandler
    public void onUserInteractWorld(UserInteractWorldEvent<User> event) {
        if(!event.hasBlock()) return;

        switch(event.getClickedBlock().getType()) {
            case SNOW_BLOCK:
                if(event.getInteractType() == UserInteractEvent.InteractType.LEFT_CLICK) event.setCancelled(false);
                return;
            case STONE_BUTTON:
                if(event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;

                Arena arena = queueButtons.get(event.getClickedBlock().getLocation().toVector());

                arena.addUserToQueue(event.getUser());
        }
    }

    private class Arena {
        private List<Vector> queueButtons;
        private List<Vector> spawnLocations;
        private Vector exitLocation;
        private BoundingBox snowBounds;
        private int extraRadius;

        private List<UUID> usersInSpleef = new ArrayList<>();
        private LinkedHashSet<UUID> queue = new LinkedHashSet<>();

        public Arena(ConfigurationSection config) {
            queueButtons = ConfigUtils.getVectorList(config, "queue_buttons");
            spawnLocations = ConfigUtils.getVectorList(config, "spawn_locations");
            exitLocation = ConfigUtils.getVector(config, "exit_location");
            snowBounds = ConfigUtils.getBounds(config, "snow");
            extraRadius = config.getInt("extra_radius");
        }

        public boolean checkUserInBounds(double x, double z) {
            if(x + extraRadius < snowBounds.min.getX() || x - extraRadius > snowBounds.max.getX()) return false;

            return z + extraRadius >= snowBounds.min.getZ() && z - extraRadius <= snowBounds.max.getZ();
        }

        public void spleefUserKilled(User user, boolean teleport) {
            if(!usersInSpleef.remove(user.getUuid())) return;
            gameLookups.remove(user.getUuid());

            removeUserFromSpleef(user, teleport);
            if(usersInSpleef.size() == 1) {
                User winner = user.getOther(usersInSpleef.remove(0));
                gameLookups.remove(winner.getUuid());

                removeUserFromSpleef(winner, true);
                //TODO send message

                tryStartGame(user);
            }

        }

        public void resetArena(User aUser) {
            World world = aUser.getLocation().getWorld();

            for(int x = snowBounds.min.getBlockX(); x <= snowBounds.max.getBlockX(); ++x) {
                for(int y = snowBounds.min.getBlockY(); y <= snowBounds.max.getBlockY(); ++y) {
                    for(int z = snowBounds.min.getBlockZ(); z <= snowBounds.max.getBlockZ(); ++z) {
                        world.getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                    }
                }
            }
        }

        public void addUserToQueue(User user) {
            if(gameLookups.containsKey(user.getUuid())) return;

            boolean success = queue.add(user.getUuid());
            if(!success) return;

            Arena old = queueLookups.get(user.getUuid());
            if(old != null) old.queue.remove(user.getUuid());

            queueLookups.put(user.getUuid(), this);

            //TODO message if success or not

            tryStartGame(user);
        }

        public void tryStartGame(User aUser) {
            if(!usersInSpleef.isEmpty() || queue.size() < spawnLocations.size()) return;

            resetArena(aUser);

            Iterator<UUID> iterator = queue.iterator();
            for(Vector spawn : spawnLocations) {
                UUID joiningUUID = iterator.next();
                iterator.remove();

                queueLookups.remove(joiningUUID);

                User joining = aUser.getOther(joiningUUID);

                joining.teleport(spawn);
                joining.getInventory().addItem(new ItemStack(spadeMaterial));
                joining.setGameMode(GameMode.SURVIVAL);
                usersInSpleef.add(joining.getUuid());
                gameLookups.put(joining.getUuid(), this);
            }

            //TODO message all spleef begin
        }

        @SuppressWarnings("unchecked")
        private void removeUserFromSpleef(User user, boolean teleport) {
            if(teleport) user.teleport(exitLocation);

            user.getInventory().remove(spadeMaterial);

            user.setGameMode(GameMode.ADVENTURE);

            user.setFireTicks(null, 0);
        }
    }
}
