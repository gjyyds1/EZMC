package top.gjcraft.eZMC.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import top.gjcraft.eZMC.utils.ThreadPoolManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerRescueManager implements Listener {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final boolean enabled;
    private final ThreadPoolManager threadPoolManager;
    private final Map<UUID, Boolean> downedPlayers;
    private final Map<UUID, BukkitTask> rescueTasks;
    private final Map<UUID, Player> rescuers;
    private final Map<UUID, BossBar> rescueProgressBars;
    private final double rescueRange;
    private final int rescueTime;
    private final int downedTimeout;
    private final Map<UUID, BukkitTask> downedTasks;

    public PlayerRescueManager(FileConfiguration config, Plugin plugin) {
        this.plugin = plugin;
        this.config = config;
        this.enabled = config.getBoolean("player-rescue.enabled", true);
        this.rescueRange = config.getDouble("player-rescue.rescue-range", 2.0);
        this.rescueTime = config.getInt("player-rescue.rescue-time", 5);
        this.downedTimeout = config.getInt("player-rescue.downed-timeout", 120);
        this.downedTasks = new HashMap<>();
        this.threadPoolManager = ThreadPoolManager.getInstance(plugin);
        this.downedPlayers = new HashMap<>();
        this.rescueTasks = new HashMap<>();
        this.rescuers = new HashMap<>();
        this.rescueProgressBars = new HashMap<>();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!enabled || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        double finalDamage = event.getFinalDamage();
        double currentHealth = player.getHealth();

        if (currentHealth - finalDamage <= 0 && !isPlayerDowned(player)) {
            event.setCancelled(true);
            setPlayerDowned(player, true);
            player.setHealth(1.0);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        if (isPlayerDowned(player)) {
            // 如果是倒地玩家，禁止移动
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setCancelled(true);
                return;
            }
        }

        // 检查是否有倒地的玩家在范围内
        CompletableFuture.runAsync(() -> {
            for (Map.Entry<UUID, Boolean> entry : downedPlayers.entrySet()) {
                Player downedPlayer = Bukkit.getPlayer(entry.getKey());
                if (downedPlayer == null || !entry.getValue()) continue;

                if (player.getLocation().distance(downedPlayer.getLocation()) <= rescueRange) {
                    startRescue(player, downedPlayer);
                } else {
                    cancelRescue(downedPlayer);
                }
            }
        }, threadPoolManager.getExecutorService());
    }

    private void startRescue(Player rescuer, Player downedPlayer) {
        UUID downedPlayerId = downedPlayer.getUniqueId();
        if (rescueTasks.containsKey(downedPlayerId)) return;

        BossBar progressBar = Bukkit.createBossBar(
            "正在救援 " + downedPlayer.getName(),
            BarColor.GREEN,
            BarStyle.SOLID
        );
        progressBar.addPlayer(rescuer);
        progressBar.addPlayer(downedPlayer);
        rescueProgressBars.put(downedPlayerId, progressBar);

        rescuers.put(downedPlayerId, rescuer);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            private int progress = 0;
            private final int maxProgress = rescueTime * 20; // 转换为tick

            @Override
            public void run() {
                progress++;
                progressBar.setProgress(Math.min(1.0, (double) progress / maxProgress));

                if (progress >= maxProgress) {
                    completeRescue(downedPlayer);
                }
            }
        }, 0L, 1L);

        rescueTasks.put(downedPlayerId, task);
    }

    private void cancelRescue(Player downedPlayer) {
        UUID playerId = downedPlayer.getUniqueId();
        if (rescueTasks.containsKey(playerId)) {
            rescueTasks.get(playerId).cancel();
            rescueTasks.remove(playerId);

            BossBar progressBar = rescueProgressBars.get(playerId);
            if (progressBar != null) {
                progressBar.removeAll();
                rescueProgressBars.remove(playerId);
            }

            rescuers.remove(playerId);
        }
    }

    private void completeRescue(Player player) {
        UUID playerId = player.getUniqueId();
        setPlayerDowned(player, false);
        player.setHealth(player.getMaxHealth() * 0.5); // 恢复50%的生命值

        cancelRescue(player);
    }

    public boolean isPlayerDowned(Player player) {
        return downedPlayers.getOrDefault(player.getUniqueId(), false);
    }

    private void setPlayerDowned(Player player, boolean downed) {
        UUID playerId = player.getUniqueId();
        downedPlayers.put(playerId, downed);

        if (downed) {
            // 开始倒地计时
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (isPlayerDowned(player)) {
                    handleGiveup(player);
                }
            }, downedTimeout * 20L);
            downedTasks.put(playerId, task);
        } else {
            // 取消倒地计时
            BukkitTask task = downedTasks.remove(playerId);
            if (task != null) {
                task.cancel();
            }
        }
    }

    public void handleGiveup(Player player) {
        UUID playerId = player.getUniqueId();
        setPlayerDowned(player, false);
        cancelRescue(player);
        player.setHealth(0); // 直接死亡
    }
}