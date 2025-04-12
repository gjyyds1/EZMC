package top.gjcraft.eZMC.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigUpdater {
    private final JavaPlugin plugin;
    private final File configFile;
    private final FileConfiguration config;
    private final double currentVersion;

    public ConfigUpdater(JavaPlugin plugin, File configFile, FileConfiguration config, double currentVersion) {
        this.plugin = plugin;
        this.configFile = configFile;
        this.config = config;
        this.currentVersion = currentVersion;
    }

    public boolean checkAndUpdate() {
        double configVersion = config.getDouble("config-version", 0.0);
        if (configVersion < currentVersion) {
            plugin.getLogger().warning("检测到配置文件版本过低或不存在版本信息，正在更新配置文件...");
            return updateConfig(configVersion);
        }
        return false;
    }

    private boolean updateConfig(double oldVersion) {
        // 创建备份文件
        File backupFile = new File(plugin.getDataFolder(), "config_backup_" + oldVersion + ".yml");
        try {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("备份配置文件时发生错误: " + e.getMessage());
            return false;
        }

        // 加载默认配置
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream == null) {
            plugin.getLogger().warning("无法加载默认配置文件");
            return false;
        }

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
            new java.io.InputStreamReader(defaultConfigStream));

        // 合并配置
        mergeConfigs(defaultConfig);

        // 保存更新后的配置
        try {
            config.save(configFile);
            plugin.getLogger().info("配置文件已更新到最新版本 " + currentVersion);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("保存更新后的配置文件时发生错误: " + e.getMessage());
            return false;
        }
    }

    private void mergeConfigs(FileConfiguration defaultConfig) {
        // 递归合并配置节点
        for (String key : defaultConfig.getKeys(true)) {
            if (!config.contains(key)) {
                // 如果配置项不存在，直接添加
                config.set(key, defaultConfig.get(key));
            } else if (defaultConfig.isConfigurationSection(key)) {
                // 如果是配置节点，继续递归处理
                continue;
            } else if (!key.equals("config-version")) {
                // 对于非配置节点且非版本号的项，检查值类型是否匹配
                Object defaultValue = defaultConfig.get(key);
                Object currentValue = config.get(key);
                
                if (defaultValue != null && currentValue != null 
                    && !defaultValue.getClass().equals(currentValue.getClass())) {
                    // 如果类型不匹配，使用默认值
                    config.set(key, defaultValue);
                    plugin.getLogger().warning("配置项 " + key + " 的值类型不正确，已重置为默认值");
                }
            }
        }
        // 更新版本号
        config.set("config-version", currentVersion);
    }
}