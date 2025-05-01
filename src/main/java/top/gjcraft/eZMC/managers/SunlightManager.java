package top.gjcraft.eZMC.managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SunlightManager {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final int checkInterval;
    private final int burnDelay;
    private final int burnDuration;
    private final double triggerChance;
    private final Set<Material> transparentBlocks;
    private final ThreadPoolManager threadPoolManager;
    private final Random random;
    private boolean isEventActive = false;

    public SunlightManager(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("sunlight-event.enabled", true);
        this.checkInterval = config.getInt("sunlight-event.check-interval", 300);
        this.burnDelay = config.getInt("sunlight-event.burn-delay", 3);
        this.burnDuration = config.getInt("sunlight-event.burn-duration", 30);
        this.triggerChance = config.getDouble("sunlight-event.trigger-chance", 0.3);
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
        this.random = new Random();

        // 初始化透明方块列表
        this.transparentBlocks = new HashSet<>();
        List<String> transparentBlocksList = config.getStringList("sunlight-event.transparent-blocks");
        for (String blockName : transparentBlocksList) {
            try {
                transparentBlocks.add(Material.valueOf(blockName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的方块类型: " + blockName);
            }
        }

        // 添加默认透明方块
        if (transparentBlocks.isEmpty()) {
            transparentBlocks.add(Material.AIR);
            transparentBlocks.add(Material.GLASS);
            transparentBlocks.add(Material.GLASS_PANE);
        }
    }

    public void startSunlightTask() {
        if (!enabled) return;

        ScheduledExecutorService scheduler = threadPoolManager.getScheduledExecutorService();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!isEventActive && random.nextDouble() < triggerChance) {
                    activateSunlightEvent();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, checkInterval, TimeUnit.SECONDS);
    }

    private void activateSunlightEvent() {
        isEventActive = true;
        plugin.getServer().broadcastMessage(ChatColor.RED + "警告：烈日凌空！请寻找遮蔽物！");

        // 启动检查玩家是否在阳光下的任务
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                checkAndBurnPlayer(player);
            }
        }, burnDelay * 20L, 20L);

        // 设置事件结束时间
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isEventActive = false;
            plugin.getServer().broadcastMessage(ChatColor.GREEN + "烈日事件结束了！");
        }, burnDuration * 20L);
    }

    private void checkAndBurnPlayer(Player player) {
        if (!isEventActive || player.isDead() || !player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            return;
        }

        // 检查玩家所在世界是否是白天
        long time = player.getWorld().getTime();
        if (time < 0 || time > 12000) {
            return;
        }

        // 检查玩家上方是否有遮挡
        Block block = player.getLocation().getBlock();
        boolean isExposed = true;

        for (int y = block.getY() + 1; y < player.getWorld().getMaxHeight(); y++) {
            Block blockAbove = player.getWorld().getBlockAt(block.getX(), y, block.getZ());
            if (!transparentBlocks.contains(blockAbove.getType())) {
                isExposed = false;
                break;
            }
        }

        // 如果玩家暴露在阳光下，则点燃玩家
        if (isExposed && !player.isInWater()) {
            player.setFireTicks(60); // 点燃3秒
        }
    }
}