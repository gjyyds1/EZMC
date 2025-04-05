package top.gjcraft.eZMC.listeners;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorldTimeListener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final long fixedTime;
    private final ThreadPoolManager threadPoolManager;

    public WorldTimeListener(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("world-settings.eternal-night.enabled", true);
        this.fixedTime = config.getLong("world-settings.eternal-night.fixed-time", 18000L);
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
    }

    public void startTimeTask() {
        if (!enabled) return;

        ScheduledExecutorService scheduler = threadPoolManager.getScheduledExecutorService();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (World world : plugin.getServer().getWorlds()) {
                        world.setTime(fixedTime);
                        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}