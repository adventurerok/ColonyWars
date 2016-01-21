package com.ithinkrok.cw.kit;

import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.metadata.UserMetadata;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 21/01/16.
 */
public class VampireListener implements Listener {

    User user;

    Calculator flightDecreaseAmount;
    double limitBlocksAboveGround;
    double blocksAboveGroundDivisor;

    double flySpeed;
    double regainFlightLevel;

    double flightLossOnDamage;
    double flightGainOnRegen;
    double flightGainPerTick;

    String unlockLocale, timeoutLocale, cooldownLocale;

    String unlockBuilding;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<User> event) {
        user = event.getCreator();

        ConfigurationSection config = event.getConfig();

        unlockBuilding = config.getString("unlock_building");

        flightDecreaseAmount = new ExpressionCalculator(config.getString("flight_decrease_amount"));
        limitBlocksAboveGround = config.getDouble("limit_blocks_above_ground");
        blocksAboveGroundDivisor = config.getDouble("blocks_above_ground_divisor");

        flySpeed = config.getDouble("fly_speed");
        regainFlightLevel = config.getDouble("regain_flight_level");

        unlockLocale = config.getString("unlock_locale", "bat_flight.unlock");
        timeoutLocale = config.getString("timeout_locale", "bat_flight.timeout");
        cooldownLocale = config.getString("cooldown_locale", "bat_flight.cooldown");

        flightLossOnDamage = config.getDouble("flight_loss_on_damage");
        flightGainOnRegen = config.getDouble("flight_gain_on_regen") / 20d;
        flightGainPerTick = config.getDouble("flight_gain_per_second") / 20d;

    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.LOW)
    public void onUserInteract(UserInteractEvent event) {
        if(event.getUser().isFlying()) event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onBuildingBuilt(BuildingBuiltEvent event) {
        VampireMetadata metadata = user.getMetadata(VampireMetadata.class);

        if(metadata == null) {
            user.setMetadata(metadata = new VampireMetadata());
            metadata.start();
        }
    }

    private class VampireMetadata extends UserMetadata implements GameRunnable {

        @Override
        public boolean removeOnInGameChange(UserInGameChangeEvent event) {
            return true;
        }

        @Override
        public boolean removeOnGameStateChange(GameStateChangedEvent event) {
            return false;
        }

        @Override
        public boolean removeOnMapChange(MapChangedEvent event) {
            return true;
        }

        public void start() {
            GameTask task = user.repeatInFuture(this, 1, 1);

            bindTaskToMetadata(task);
        }

        private boolean bat = false;
        private boolean allowFlight = false;
        private boolean mageTower = false;
        private double oldHealth = 0;
        private int blocksAboveGround = 0;
        private int maxBlocksAboveGround = 0;

        @Override
        public void run(GameTask task) {
            if (!mageTower) {
                CWTeamStats teamStats = CWTeamStats.getOrCreate(user.getTeam());

                if (teamStats.getBuildingCount(unlockBuilding) < 1) return;
                mageTower = true;
                user.setExp(1f);
                user.sendLocale(unlockLocale);

                oldHealth = user.getHealth();
            }

            blocksAboveGround = calculateBlocksAboveGround();

            double newHealth = user.getHealth();
            float change = 0f;

            //Change due to damage taken
            if (newHealth < oldHealth) change -= flightLossOnDamage;

            //Change due to regeneration
            if (user.hasPotionEffect(PotionEffectType.REGENERATION)) change += flightGainOnRegen;

            if (change != 0) {
                float exp = user.getExp();
                exp = Math.max(Math.min(exp + change, 1f), 0f);
                user.setExp(exp);
            }

            oldHealth = newHealth;

            Block block = user.getLocation().getBlock();
            boolean inWater = block.getRelative(0, 1, 0).isLiquid() ||
                    (block.isLiquid() && block.getRelative(0, -1, 0).isLiquid());

            if (user.isFlying() && !inWater) {
                float exp = user.getExp();
                exp = Math.max(exp - flightDecreaseAmount(), 0);
                user.setExp(exp);
                user.setFallDistance(0);

                if (!bat) {
                    user.disguise(EntityType.BAT);
                    bat = true;
                }

                if (exp > 0) return;
                user.setAllowFlight(allowFlight = false);
                user.sendLocale(timeoutLocale);
            } else {
                if (inWater) user.setFlying(false);

                if(!user.isOnGround()) {
                    if(blocksAboveGround > maxBlocksAboveGround) maxBlocksAboveGround = blocksAboveGround;
                    else if(maxBlocksAboveGround > 3 && blocksAboveGround < 2) user.setAllowFlight(allowFlight = false);
                }

                float exp = user.getExp();
                //Default change
                exp = (float) Math.min(exp + flightGainPerTick, 1);
                user.setExp(exp);


                if (exp > regainFlightLevel && !allowFlight && user.isOnGround()) {
                    user.setAllowFlight(allowFlight = true);
                    if(maxBlocksAboveGround == 0) user.sendLocale(cooldownLocale);
                    user.setFlySpeed(flySpeed);
                }

                if(user.isOnGround()) maxBlocksAboveGround = 0;

                if (bat) {
                    user.unDisguise();
                    bat = false;
                }
            }
        }

        public int calculateBlocksAboveGround() {
            int yMod = 0;
            Block block = user.getLocation().getBlock();
            while (block.getLocation().getBlockY() - yMod > 1) {
                ++yMod;

                if (block.getRelative(0, -yMod, 0).getType().isSolid()) break;
            }

            return yMod;
        }

        private float flightDecreaseAmount() {
            double base =  flightDecreaseAmount.calculate(user.getUpgradeLevels());

            if(blocksAboveGround >= limitBlocksAboveGround) base *= (blocksAboveGround / blocksAboveGroundDivisor);

            return (float) base;
        }
    }
}
