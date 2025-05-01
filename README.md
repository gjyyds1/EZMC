# EZMC - Minecraft难度增强插件

EZMC是一个为Minecraft服务器提供多种难度提升机制的Bukkit/Spigot插件，让你的Minecraft之旅更具挑战性。通过独特的机制设计，为玩家带来全新的游戏体验。

## 主要特性

### 1. 永夜系统

- 开启后世界将永远处于夜晚状态
- 可通过配置文件自定义固定时间
- 为玩家创造持续的黑暗挑战环境

### 2. 灾厄之夜

- 在特定天数后触发的特殊事件
- 持续多天的强化怪物挑战
- 怪物属性全面提升（生命值、攻击力、速度）
- 更多的怪物生成数量
- 需要团队合作才能更好地生存

### 3. 烈日凌空事件

- 随机触发的天气事件系统
- 暴露在阳光下的玩家将受到伤害
- 需要寻找庇护所或使用特殊道具进行防护
- 增加白天活动的风险性

### 4. 怪物增强系统

- 全局怪物属性提升
- 可自定义生命值、攻击力和移动速度倍率
- 更具挑战性的战斗体验
- 怪物AI优化，更智能的追踪机制

### 5. 倒地系统

- 玩家血量耗尽后进入倒地状态
- 队友可以救援倒地玩家
- 使用/ezmc giveup放弃救援
- 增加团队合作的必要性

## 命令说明

- `/ezmc giveup` - 在倒地状态下放弃，直接死亡
- `/ezmc reload` - 重新加载插件配置（需要权限：ezmc.admin.reload）

## 权限节点

- `ezmc.admin.reload` - 允许重载插件配置（默认OP）

## 配置指南

配置文件位于 `plugins/EZMC/config.yml`

### 主要配置项

```yaml
# 永夜设置
world-settings:
  eternal-night:
    enabled: true
    fixed-time: 18000  # 设置为18000为永夜

# 灾厄之夜设置
doom-night:
  enabled: true
  start-day: 10        # 开始触发的天数
  duration: 3          # 持续天数
  mob-health-multiplier: 2.0
  mob-damage-multiplier: 2.0
  mob-speed-multiplier: 1.5
  spawn-amount-multiplier: 2.0

# 烈日凌空事件设置
sunlight-event:
  enabled: true
  check-interval: 300  # 检查间隔（秒）
  burn-delay: 3        # 燃烧延迟（秒）
  burn-duration: 30    # 事件持续时间（秒）
  trigger-chance: 0.3  # 触发概率

# 怪物增强设置
mob-enhancement:
  enabled: true
  health-multiplier: 1.5
  damage-multiplier: 1.3
  speed-multiplier: 1.2
```

## 安装步骤

1. 下载最新版本的EZMC.jar
2. 将插件放入服务器的plugins文件夹
3. 重启服务器或重载插件
4. 首次启动时会自动生成配置文件

## 注意事项

- 请定期备份服务器数据
- 合理配置各项参数，避免游戏难度过高
- 建议在测试环境中先进行功能测试
- 确保服务器版本与插件版本兼容

## 版本历史

### v1.3.0-SNAPSHOT

- 新增烈日凌空事件系统
- 优化灾厄之夜机制
- 改进数据持久化存储
- 优化怪物AI行为
- 完善配置文件结构

## 关于

- 作者：gjyyds1
- 版本：1.3.0-SNAPSHOT
- API版本：1.21

## 许可证

本项目采用MIT许可证。详见[LICENSE](LICENSE)文件。