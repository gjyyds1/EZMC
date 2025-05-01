package top.gjcraft.eZMC.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.concurrent.CompletableFuture;

public class MobEnhancementListener implements Listener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final double healthMultiplier;
    private final double damageMultiplier;
    private final double speedMultiplier;
    private final ThreadPoolManager threadPoolManager;

    public MobEnhancementListener(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("mob-enhancement.enabled");
        this.healthMultiplier = config.getDouble("mob-enhancement.health-multiplier");
        this.damageMultiplier = config.getDouble("mob-enhancement.damage-multiplier");
        this.speedMultiplier = config.getDouble("mob-enhancement.speed-multiplier");
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!enabled || !(event.getEntity() instanceof Monster)) {
            return;
        }

        LivingEntity entity = event.getEntity();

        // 异步计算属性值
        CompletableFuture.runAsync(() -> {
            // 计算新的属性值
            double baseHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            double newHealth = baseHealth * healthMultiplier;

            double baseDamage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
            double newDamage = baseDamage * damageMultiplier;

            double baseSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
            double newSpeed = baseSpeed * speedMultiplier;

            // 在主线程中更新实体属性
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 更新生命值
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
                entity.setHealth(newHealth);

                // 更新攻击力
                entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(newDamage);

                // 更新移动速度
                entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(newSpeed);
            });
        }, threadPoolManager.getExecutorService());
    }
}