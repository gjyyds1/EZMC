package top.gjcraft.eZMC.listeners;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.gjcraft.eZMC.managers.DoomNightManager;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorldTimeListener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final long fixedTime;
    private final ThreadPoolManager threadPoolManager;
    private final DoomNightManager doomNightManager;
    private boolean isEternalNightActive = false;

    public WorldTimeListener(FileConfiguration config, Plugin plugin, DoomNightManager doomNightManager) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("world-settings.eternal-night.enabled", true);
        this.fixedTime = config.getLong("world-settings.eternal-night.fixed-time", 18000L);
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
        this.doomNightManager = doomNightManager;
    }

    public void startTimeTask() {
        if (!enabled) return;

        ScheduledExecutorService scheduler = threadPoolManager.getScheduledExecutorService();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (World world : plugin.getServer().getWorlds()) {
                        // 检查是否处于灾厄之夜状态
                        if (doomNightManager.isDoomNightActive() && !isEternalNightActive) {
                            world.setTime(18000L);
                            world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
                            isEternalNightActive = true;
                        }
                        // 如果不是灾厄之夜，则按照原有的永夜设置处理
                        else if (!doomNightManager.isDoomNightActive()) {
                            if (isEternalNightActive) {
                                world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, true);
                                isEternalNightActive = false;
                            }
                            world.setTime(fixedTime);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}