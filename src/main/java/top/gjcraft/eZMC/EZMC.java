package top.gjcraft.eZMC;

import org.bukkit.plugin.java.JavaPlugin;
import top.gjcraft.eZMC.commands.EZMCCommand;
import top.gjcraft.eZMC.config.ConfigUpdater;
import top.gjcraft.eZMC.listeners.*;
import top.gjcraft.eZMC.managers.DoomNightManager;
import top.gjcraft.eZMC.managers.SunlightManager;

import java.io.File;

public final class EZMC extends JavaPlugin {

    @Override
    public void onEnable() {
        // 保存默认配置文件
        saveDefaultConfig();
        
        // 检查并更新配置文件
        File configFile = new File(getDataFolder(), "config.yml");
        ConfigUpdater configUpdater = new ConfigUpdater(this, configFile, getConfig(), 1.0);
        if (configUpdater.checkAndUpdate()) {
            reloadConfig();
        }

        // 创建PlayerRescueManager实例
        PlayerRescueManager rescueManager = new PlayerRescueManager(getConfig(), this);

        // 创建DoomNightManager和SunlightManager实例
        DoomNightManager doomNightManager = new DoomNightManager(getConfig(), this);
        SunlightManager sunlightManager = new SunlightManager(getConfig(), this);

        // 注册监听器
        getServer().getPluginManager().registerEvents(new MobEnhancementListener(getConfig(), this), this);
        getServer().getPluginManager().registerEvents(new EnvironmentHazardsListener(getConfig(), this), this);
        getServer().getPluginManager().registerEvents(new PlayerDebuffsListener(getConfig(), this), this);
        getServer().getPluginManager().registerEvents(new BlockDropListener(getConfig(), this), this);
        getServer().getPluginManager().registerEvents(rescueManager, this);
        getServer().getPluginManager().registerEvents(new PlayerHealthManager(getConfig(), this, rescueManager), this);

        // 注册命令执行器
        getCommand("ezmc").setExecutor(new EZMCCommand(this, rescueManager));

        // 初始化并启动怪物生成任务
        MobSpawnListener mobSpawnListener = new MobSpawnListener(getConfig(), this, doomNightManager);
        mobSpawnListener.startSpawnTask();

        // 注册怪物追踪火把玩家的监听器
        getServer().getPluginManager().registerEvents(new MobTorchTrackingListener(getConfig(), this), this);

        // 初始化并启动永夜任务和灾厄之夜任务
        WorldTimeListener worldTimeListener = new WorldTimeListener(getConfig(), this, doomNightManager);
        worldTimeListener.startTimeTask();
        doomNightManager.startDoomNightTask();

        // 启动烈日凌空事件任务
        sunlightManager.startSunlightTask();

        getLogger().info("EZMC插件已启动");
        getLogger().info("作者: gjyyds1");
        getLogger().info("难度增强系统已加载");
    }

    @Override
    public void onDisable() {
        getLogger().info("EZMC插件已关闭");
        saveConfig();
    }
}
