package com.ithinkrok.cw.metadata;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.UserMetadata;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by paul on 16/01/16.
 */
public class ShowdownArena extends Metadata {

    private final GameGroup gameGroup;

    private int radiusX, radiusZ;
    private final Location center;
    private final BoundingBox bounds;

    private final int minRadius;
    private final int shrinkStartTime;
    private final int shrinkIntervalTime;

    private final String showdownShrinkingLocale;

    public ShowdownArena(GameGroup gameGroup) {
        this.gameGroup = gameGroup;

        Config config = gameGroup.getSharedObject("showdown");
        this.radiusX = config.getInt("size.x");
        this.radiusZ = config.getInt("size.z");

        MapPoint center = MinigamesConfigs.getMapPoint(config, "center");
        this.center = gameGroup.getCurrentMap().getLocation(center);

        MapPoint min = center.add(-radiusX - 5, 0, -radiusZ - 5);
        MapPoint max = center.add(radiusX + 5, 256, radiusZ + 5);

        this.bounds = new BoundingBox(min.getXYZ(), max.getXYZ());

        Config metadata = gameGroup.getSharedObject("showdown_metadata");

        minRadius = metadata.getInt("min_radius", 5);
        shrinkStartTime = (int) (metadata.getDouble("shrink_start_time", 180) * 20);
        shrinkIntervalTime = (int) (metadata.getDouble("shrink_interval_time", 20) * 20);

        showdownShrinkingLocale = metadata.getString("shrink_start_locale", "showdown.shrinking");
    }

    public int getRadiusX() {
        return radiusX;
    }

    public int getRadiusZ() {
        return radiusZ;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public Location getCenter() {
        return center;
    }

    public void startShrinkTask() {
        GameTask announceTask =
                gameGroup.doInFuture(task -> gameGroup.sendLocale(showdownShrinkingLocale), shrinkStartTime);
        gameGroup.bindTaskToCurrentGameState(announceTask);

        GameTask shrinkTask = gameGroup.repeatInFuture(t -> shrinkArena(), shrinkStartTime, shrinkIntervalTime);
        gameGroup.bindTaskToCurrentGameState(shrinkTask);
    }

    private void shrinkArena() {
        if (radiusX == minRadius && radiusZ == minRadius) return;

        if (radiusX > minRadius) --radiusX;
        if (radiusZ > minRadius) --radiusZ;
    }

    public void startCheckTasks() {
        for (User user : gameGroup.getUsers()) {
            if (!user.isInGame()) continue;

            GameTask task = user.repeatInFuture(t -> checkUserMove(user, user.getLocation()), 5, 5);
            user.bindTaskToInGame(task);
            gameGroup.bindTaskToCurrentGameState(task);
        }
    }

    private boolean checkUserMove(User user, Location target) {
        if (isInBounds(target)){
            user.removeMetadata(OutsideShowdownTracker.class);
            return false;
        }

        double xv = 0;
        if (target.getX() > center.getX() + radiusX - 2) xv = -1;
        else if (target.getX() < center.getX() - radiusZ + 2) xv = 1;

        double zv = 0;
        if (target.getZ() > center.getZ() + radiusZ - 2) zv = -1;
        else if (target.getZ() < center.getZ() - radiusZ + 2) zv = 1;

        Vector velocity = new Vector();

        velocity.setX(velocity.getX() + xv * 0.25);
        velocity.setY(0.1);
        velocity.setZ(velocity.getZ() + zv * 0.25);

        if (!user.isInsideVehicle()) user.setVelocity(velocity);
        else user.getVehicle().setVelocity(velocity);

        user.showAboveHotbarLocale("showdown.escape");

        OutsideShowdownTracker tracker;
        if(user.hasMetadata(OutsideShowdownTracker.class)) {
            tracker = user.getMetadata(OutsideShowdownTracker.class);
        } else{
            tracker = new OutsideShowdownTracker();
            user.setMetadata(tracker);
        }

        tracker.ticksOutsideShowdown += 5;

        if(tracker.ticksOutsideShowdown > 200) {
            user.teleport(center);
            user.removeMetadata(OutsideShowdownTracker.class);
        }

        return true;
    }

    private boolean isInBounds(Location loc) {
        double xd = Math.abs(loc.getX() - center.getX());
        double zd = Math.abs(loc.getZ() - center.getZ());

        return !(xd > radiusX || zd > radiusZ);
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }

    private static class OutsideShowdownTracker extends UserMetadata {

        public int ticksOutsideShowdown = 0;

        @Override
        public boolean removeOnInGameChange(UserInGameChangeEvent event) {
            return true;
        }

        @Override
        public boolean removeOnGameStateChange(GameStateChangedEvent event) {
            return true;
        }

        @Override
        public boolean removeOnMapChange(MapChangedEvent event) {
            return true;
        }
    }
}
