package top.gjcraft.eZMC.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import top.gjcraft.eZMC.listeners.PlayerRescueManager;

public class EZMCCommand implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerRescueManager rescueManager;

    public EZMCCommand(Plugin plugin, PlayerRescueManager rescueManager) {
        this.plugin = plugin;
        this.rescueManager = rescueManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /ezmc <giveup|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "giveup":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行！");
                    return true;
                }
                Player player = (Player) sender;
                if (!rescueManager.isPlayerDowned(player)) {
                    player.sendMessage(ChatColor.RED + "你现在不处于倒地状态！");
                    return true;
                }
                rescueManager.handleGiveup(player);
                return true;

            case "reload":
                if (!sender.hasPermission("ezmc.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "配置文件已重新加载！");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "未知的子命令！");
                return true;
        }
    }
}