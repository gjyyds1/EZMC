package top.gjcraft.eZMC.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.concurrent.CompletableFuture;

public class EnvironmentHazardsListener implements Listener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final ThreadPoolManager threadPoolManager;
    private final double fallDamageMultiplier;
    private final double fireDamageMultiplier;
    private final double hungerRateMultiplier;

    public EnvironmentHazardsListener(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("environment-hazards.enabled");
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
        this.fallDamageMultiplier = config.getDouble("environment-hazards.fall-damage-multiplier");
        this.fireDamageMultiplier = config.getDouble("environment-hazards.fire-damage-multiplier");
        this.hungerRateMultiplier = config.getDouble("environment-hazards.hunger-rate-multiplier");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!enabled || !(event.getEntity() instanceof Player)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            EntityDamageEvent.DamageCause cause = event.getCause();
            double damage = event.getDamage();
            double finalDamage = damage;

            switch (cause) {
                case FALL:
                    finalDamage = damage * fallDamageMultiplier;
                    break;
                case FIRE:
                case FIRE_TICK:
                case LAVA:
                    finalDamage = damage * fireDamageMultiplier;
                    break;
            }

            double damageToApply = finalDamage;
            plugin.getServer().getScheduler().runTask(plugin, () -> event.setDamage(damageToApply));
        }, threadPoolManager.getExecutorService());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!enabled || !(event.getEntity() instanceof Player)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            if (event.getFoodLevel() < ((Player) event.getEntity()).getFoodLevel()) {
                int foodLevelChange = ((Player) event.getEntity()).getFoodLevel() - event.getFoodLevel();
                int newFoodLevel = event.getFoodLevel() - (int) Math.ceil(foodLevelChange * (hungerRateMultiplier - 1));
                plugin.getServer().getScheduler().runTask(plugin, () -> event.setFoodLevel(newFoodLevel));
            }
        }, threadPoolManager.getExecutorService());
    }
}