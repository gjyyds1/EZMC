package top.gjcraft.eZMC.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.concurrent.CompletableFuture;

public class PlayerHealthManager implements Listener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final double initialHealth;
    private final int levelIncrement;
    private final double healthPerIncrement;
    private final double maxHealth;
    private final ThreadPoolManager threadPoolManager;
    private final PlayerRescueManager rescueManager;

    public PlayerHealthManager(FileConfiguration config, Plugin plugin, PlayerRescueManager rescueManager) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("player-health.enabled", true);
        this.initialHealth = config.getDouble("player-health.initial-health", 6.0);
        this.levelIncrement = config.getInt("player-health.level-increment", 5);
        this.healthPerIncrement = config.getDouble("player-health.health-per-increment", 2.0);
        this.maxHealth = config.getDouble("player-health.max-health", 20.0);
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
        this.rescueManager = rescueManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enabled) return;
        updatePlayerMaxHealth(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        if (!enabled) return;
        updatePlayerMaxHealth(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!enabled || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        double finalDamage = event.getFinalDamage();
        double currentHealth = player.getHealth();

        // 如果玩家已经处于倒地状态，不进行处理
        if (rescueManager.isPlayerDowned(player)) {
            return;
        }

        // 如果伤害会导致玩家死亡，触发倒地机制
        if (currentHealth - finalDamage <= 0) {
            event.setCancelled(true);
            player.setHealth(1.0);
        }
    }

    private void updatePlayerMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) return;

        // 异步计算新的最大生命值
        CompletableFuture.runAsync(() -> {
            int level = player.getLevel();
            int increments = level / levelIncrement;
            double newMaxHealth = initialHealth + (increments * healthPerIncrement);
            
            // 确保不超过配置的最大值
            double finalMaxHealth = Math.min(newMaxHealth, maxHealth);
            
            // 在主线程中更新玩家属性
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置新的最大生命值
                attribute.setBaseValue(finalMaxHealth);
                
                // 如果当前生命值超过新的最大值，将其调整为最大值
                if (player.getHealth() > finalMaxHealth) {
                    player.setHealth(finalMaxHealth);
                }
            });
        }, threadPoolManager.getExecutorService());
    }
}