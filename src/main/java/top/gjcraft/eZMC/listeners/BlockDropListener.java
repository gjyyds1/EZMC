package top.gjcraft.eZMC.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class BlockDropListener implements Listener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final Random random;
    private final ThreadPoolManager threadPoolManager;

    public BlockDropListener(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.random = new Random();
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.getBoolean("block-drops.enabled", true)) {
            return;
        }

        Material blockType = event.getBlock().getType();
        String configPath = "block-drops.materials." + blockType.name().toLowerCase();

        // 异步计算掉落概率和数量
        CompletableFuture.runAsync(() -> {
            // 如果该方块类型没有配置掉落概率，使用默认概率
            double dropChance = config.getDouble(configPath, config.getDouble("block-drops.default-chance", 1.0));

            // 如果随机数大于掉落概率，取消掉落
            if (random.nextDouble() > dropChance) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    event.setDropItems(false);
                });
                return;
            }

            // 如果设置了特定的掉落数量控制
            if (config.contains(configPath + ".amount")) {
                // 在主线程中取消原始掉落并获取掉落物
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    event.setDropItems(false);
                    Collection<ItemStack> drops = event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand());

                    // 异步计算每个掉落物的数量
                    for (ItemStack drop : drops) {
                        int originalAmount = drop.getAmount();
                        int minAmount = config.getInt(configPath + ".amount.min", originalAmount);
                        int maxAmount = config.getInt(configPath + ".amount.max", originalAmount);

                        // 确保最小值不大于最大值
                        minAmount = Math.min(minAmount, maxAmount);

                        // 在最小值和最大值之间随机选择一个数量
                        int finalAmount = minAmount;
                        if (maxAmount > minAmount) {
                            finalAmount += random.nextInt(maxAmount - minAmount + 1);
                        }

                        if (finalAmount > 0) {
                            final int dropAmount = finalAmount;
                            ItemStack finalDrop = drop.clone();
                            finalDrop.setAmount(dropAmount);
                            // 在主线程中生成掉落物
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), finalDrop);
                            });
                        }
                    }
                });
            }
        }, threadPoolManager.getExecutorService());
    }
}