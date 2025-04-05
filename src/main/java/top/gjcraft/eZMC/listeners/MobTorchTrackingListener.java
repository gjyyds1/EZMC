package top.gjcraft.eZMC.listeners;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.concurrent.CompletableFuture;

public class MobTorchTrackingListener implements Listener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final double trackingRange;
    private final double trackingSpeedMultiplier;
    private final ThreadPoolManager threadPoolManager;

    public MobTorchTrackingListener(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("mob-spawn.torch-tracking.enabled", true);
        this.trackingRange = config.getDouble("mob-spawn.torch-tracking.tracking-range", 32.0);
        this.trackingSpeedMultiplier = config.getDouble("mob-spawn.torch-tracking.tracking-speed-multiplier", 1.5);
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!enabled || !(event.getEntity() instanceof Monster)) {
            return;
        }

        Monster monster = (Monster) event.getEntity();
        
        CompletableFuture.runAsync(() -> {
            // 在主线程中设置怪物的视野范围
            plugin.getServer().getScheduler().runTask(plugin, () -> 
                monster.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(trackingRange));

            // 寻找附近手持火把的玩家
            Player nearestTorchPlayer = null;
            double nearestDistance = trackingRange;

            for (Player player : monster.getWorld().getPlayers()) {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                ItemStack offHand = player.getInventory().getItemInOffHand();

                if (mainHand.getType() == Material.TORCH || offHand.getType() == Material.TORCH) {
                    double distance = monster.getLocation().distance(player.getLocation());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestTorchPlayer = player;
                    }
                }
            }

            // 如果找到手持火把的玩家，在主线程中设置目标并增加移动速度
            if (nearestTorchPlayer != null) {
                Player targetPlayer = nearestTorchPlayer;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    monster.setTarget(targetPlayer);
                    double baseSpeed = monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
                    monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(baseSpeed * trackingSpeedMultiplier);
                });
            }
        }, threadPoolManager.getExecutorService());
    }
}