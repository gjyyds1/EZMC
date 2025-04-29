package top.gjcraft.eZMC.managers;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.Plugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DoomNightManager {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final int startDay;
    private final int duration;
    private final double mobHealthMultiplier;
    private final double mobDamageMultiplier;
    private final double mobSpeedMultiplier;
    private final double spawnAmountMultiplier;
    private final ThreadPoolManager threadPoolManager;
    private boolean isDoomNightActive = false;

    public DoomNightManager(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("doom-night.enabled", true);
        this.startDay = config.getInt("doom-night.start-day", 10);
        this.duration = config.getInt("doom-night.duration", 3);
        this.mobHealthMultiplier = config.getDouble("doom-night.mob-health-multiplier", 2.0);
        this.mobDamageMultiplier = config.getDouble("doom-night.mob-damage-multiplier", 2.0);
        this.mobSpeedMultiplier = config.getDouble("doom-night.mob-speed-multiplier", 1.5);
        this.spawnAmountMultiplier = config.getDouble("doom-night.spawn-amount-multiplier", 2.0);
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
    }

    public void startDoomNightTask() {
        if (!enabled) return;

        ScheduledExecutorService scheduler = threadPoolManager.getScheduledExecutorService();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (World world : plugin.getServer().getWorlds()) {
                        long totalDays = world.getFullTime() / 24000L;
                        if (totalDays >= startDay) {
                            long daysPassed = (totalDays - startDay) % (duration + 7);
                            if (daysPassed < duration) {
                                if (!isDoomNightActive) {
                                    activateDoomNight();
                                }
                            } else if (isDoomNightActive) {
                                deactivateDoomNight();
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void activateDoomNight() {
        isDoomNightActive = true;
        plugin.getServer().broadcastMessage("§c灾厄之夜降临！怪物变得更加强大！");
        
        // 增强所有现存的怪物
        for (World world : plugin.getServer().getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity instanceof Monster) {
                    enhanceMob((Monster) entity);
                }
            }
        }
    }

    private void deactivateDoomNight() {
        isDoomNightActive = false;
        plugin.getServer().broadcastMessage("§a灾厄之夜结束了！");
    }

    private void enhanceMob(Monster monster) {
        // 增强怪物属性
        monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(
            monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * mobHealthMultiplier
        );
        monster.setHealth(monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(
            monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() * mobDamageMultiplier
        );

        monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
            monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * mobSpeedMultiplier
        );
    }

    public boolean isDoomNightActive() {
        return isDoomNightActive;
    }

    public double getSpawnAmountMultiplier() {
        return isDoomNightActive ? spawnAmountMultiplier : 1.0;
    }
}