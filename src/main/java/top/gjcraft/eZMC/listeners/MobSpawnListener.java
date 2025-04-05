package top.gjcraft.eZMC.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MobSpawnListener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final int spawnInterval;
    private final int spawnAmount;
    private final int spawnRadius;
    private final List<EntityType> allowedMobs;
    private final Random random;
    private final ThreadPoolManager threadPoolManager;

    public MobSpawnListener(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("mob-spawn.enabled", true);
        this.spawnInterval = config.getInt("mob-spawn.spawn-interval", 300);
        this.spawnAmount = config.getInt("mob-spawn.spawn-amount", 3);
        this.spawnRadius = config.getInt("mob-spawn.spawn-radius", 20);
        this.random = new Random();
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
        this.allowedMobs = new ArrayList<>();
        
        // 添加默认允许生成的怪物类型
        allowedMobs.add(EntityType.ZOMBIE);
        allowedMobs.add(EntityType.SKELETON);
        allowedMobs.add(EntityType.SPIDER);
        allowedMobs.add(EntityType.CREEPER);
    }

    public void startSpawnTask() {
        if (!enabled) return;

        ScheduledExecutorService scheduler = threadPoolManager.getScheduledExecutorService();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        spawnMobsAroundPlayer(player);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, spawnInterval, TimeUnit.SECONDS);
    }

    private void spawnMobsAroundPlayer(Player player) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();

        for (int i = 0; i < spawnAmount; i++) {
            // 在玩家周围随机选择一个位置
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * spawnRadius;
            double x = playerLoc.getX() + distance * Math.cos(angle);
            double z = playerLoc.getZ() + distance * Math.sin(angle);
            Location spawnLoc = new Location(world, x, playerLoc.getY(), z);

            // 找到合适的生成位置（地面上的安全位置）
            spawnLoc = world.getHighestBlockAt(spawnLoc).getLocation().add(0, 1, 0);

            // 随机选择一个怪物类型
            EntityType mobType = allowedMobs.get(random.nextInt(allowedMobs.size()));
            world.spawnEntity(spawnLoc, mobType);
        }
    }
}