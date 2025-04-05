# EZMC Plugin

EZMC是一个为Minecraft服务器提供多种难度提升机制的Bukkit/Spigot插件，让你的Minecraft之旅更具挑战性。

## 功能特性

- 怪物增强系统
  - 更高的生命值和伤害
  - 额外buff加成
- 环境挑战
  - 永夜设置
  - 随机事件
- 惩罚机制
  - 玩家倒地系统
  - 额外debuff
- 救援系统
  - 玩家互救机制
  - 倒地放弃功能
- 配置文件热重载

## 命令

- `/ezmc giveup` - 在倒地状态下放弃，直接死亡
- `/ezmc reload` - 重新加载插件配置文件（需要权限：ezmc.admin.reload）

## 权限

- `ezmc.admin.reload` - 允许重载插件配置

## 配置说明

插件的配置文件将在首次启动时自动生成。你可以根据需要修改配置文件中的参数。

## 安装

1. 下载插件的jar文件
2. 将jar文件放入服务器的plugins文件夹
3. 重启服务器或重载插件

## 开发

本项目使用Maven构建。克隆仓库后，你可以使用以下命令构建项目：

```bash
mvn clean install
```

## 许可证

本项目采用MIT许可证。详见[LICENSE](LICENSE)文件。