package top.gjcraft.eZMC.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class PlayerDebuffsListener implements Listener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final Random random;
    private final ThreadPoolManager threadPoolManager;

    // 挖掘疲劳配置
    private final boolean miningFatigueEnabled;
    private final int miningFatigueLevel;
    private final int miningFatigueDuration;
    private final double miningFatigueChance;

    // 虚弱效果配置
    private final boolean weaknessEnabled;
    private final int weaknessLevel;
    private final int weaknessDuration;
    private final double weaknessChance;

    public PlayerDebuffsListener(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("player-debuffs.enabled");
        this.random = new Random();
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);

        // 初始化挖掘疲劳配置
        this.miningFatigueEnabled = config.getBoolean("player-debuffs.mining-fatigue.enabled");
        this.miningFatigueLevel = config.getInt("player-debuffs.mining-fatigue.level");
        this.miningFatigueDuration = config.getInt("player-debuffs.mining-fatigue.duration") * 20; // 转换为tick
        this.miningFatigueChance = config.getDouble("player-debuffs.mining-fatigue.chance");

        // 初始化虚弱效果配置
        this.weaknessEnabled = config.getBoolean("player-debuffs.weakness.enabled");
        this.weaknessLevel = config.getInt("player-debuffs.weakness.level");
        this.weaknessDuration = config.getInt("player-debuffs.weakness.duration") * 20; // 转换为tick
        this.weaknessChance = config.getDouble("player-debuffs.weakness.chance");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled || !miningFatigueEnabled) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            Player player = event.getPlayer();
            if (random.nextDouble() < miningFatigueChance) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        miningFatigueDuration,
                        miningFatigueLevel - 1
                    )));
            }
        }, threadPoolManager.getExecutorService());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!enabled || !weaknessEnabled || !(event.getDamager() instanceof Player)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            Player player = (Player) event.getDamager();
            if (random.nextDouble() < weaknessChance) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.WEAKNESS,
                        weaknessDuration,
                        weaknessLevel - 1
                    )));
            }
        }, threadPoolManager.getExecutorService());
    }
}