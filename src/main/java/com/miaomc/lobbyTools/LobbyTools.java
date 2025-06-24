package com.miaomc.lobbyTools;

import com.miaomc.lobbyTools.commands.LobbyToolsCommand;
import com.miaomc.lobbyTools.listeners.CommandListener;
import com.miaomc.lobbyTools.listeners.DamageListener;
import com.miaomc.lobbyTools.listeners.PlayerListener;
import com.miaomc.lobbyTools.listeners.WorldListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LobbyTools extends JavaPlugin {
    private static LobbyTools instance;
    private Location spawnLocation;
    private boolean spawnLoaded = false;

    // MiniMessage 相关字段
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        // 保存实例以便在其他类中访问
        instance = this;

        // 初始化 Adventure API
        this.adventure = BukkitAudiences.create(this);
        this.miniMessage = MiniMessage.miniMessage();

        // 加载配置
        saveDefaultConfig();

        try {
            loadSpawnLocation();
        } catch (Exception e) {
            getLogger().severe("加载出生点出错: " + e.getMessage());
            getLogger().warning("出生点传送功能将被禁用，但插件的其他功能仍然可用。");
        }

        // 注册命令
        getCommand("lobbytools").setExecutor(new LobbyToolsCommand(this));
        getCommand("mlt").setExecutor(new LobbyToolsCommand(this));

        // 注册监听器
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        getLogger().info("LobbyTools 插件已启用!");
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        getLogger().info("LobbyTools 插件已禁用!");
    }

    // 获取 Adventure API 实例
    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Adventure API 未初始化或已关闭!");
        }
        return this.adventure;
    }

    // 获取 MiniMessage 实例
    public MiniMessage miniMessage() {
        return this.miniMessage;
    }

    // 使用 MiniMessage 解析文本
    public Component parseMiniMessage(String text) {
        return miniMessage.deserialize(text);
    }

    public static LobbyTools getInstance() {
        return instance;
    }

    public void loadSpawnLocation() {
        FileConfiguration config = getConfig();
        if (config.contains("spawn.world")) {
            String worldName = config.getString("spawn.world");
            // 检查世界是否存在
            if (getServer().getWorld(worldName) != null) {
                double x = config.getDouble("spawn.x");
                double y = config.getDouble("spawn.y");
                double z = config.getDouble("spawn.z");
                float yaw = (float) config.getDouble("spawn.yaw");
                float pitch = (float) config.getDouble("spawn.pitch");

                spawnLocation = new Location(getServer().getWorld(worldName), x, y, z, yaw, pitch);
                getLogger().info("已从配置加载出生点: " + worldName + ", X:" + x + ", Y:" + y + ", Z:" + z);
                spawnLoaded = true;
            } else {
                getLogger().warning("配置中指定的世界 '" + worldName + "' 不存在！将在世界加载后再尝试设置出生点。");
                // 不设置spawnLocation，稍后在世界加载时再处理
                spawnLoaded = false;
            }
        } else {
            // 如果没有设置，尝试使用世界默认出生点
            List<org.bukkit.World> worlds = getServer().getWorlds();
            if (worlds != null && !worlds.isEmpty()) {
                spawnLocation = worlds.get(0).getSpawnLocation();
                getLogger().info("使用默认世界出生点作为传送位置。");
                spawnLoaded = true;
            } else {
                getLogger().warning("无法获取世界列表或世界列表为空。将在世界加载后再尝试设置出生点。");
                spawnLoaded = false;
                // 不设置spawnLocation，等待世界加载
            }
        }
    }

    public void saveSpawnLocation(Location location) {
        FileConfiguration config = getConfig();
        config.set("spawn.world", location.getWorld().getName());
        config.set("spawn.x", location.getX());
        config.set("spawn.y", location.getY());
        config.set("spawn.z", location.getZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());
        saveConfig();

        spawnLocation = location;
        spawnLoaded = true;
    }

    public Location getSpawnLocation() {
        // 如果出生点还没有加载，尝试重新加载
        if (!spawnLoaded) {
            try {
                loadSpawnLocation();
            } catch (Exception e) {
                getLogger().warning("获取出生点时出错: " + e.getMessage());
            }
        }
        return spawnLocation;
    }

    public boolean isSpawnLoaded() {
        return spawnLoaded;
    }

    // 获取配置中的消息并添加颜色代码
    public String getMessage(String path) {
        String message = getConfig().getString("messages." + path);
        if (message == null) {
            return "消息未配置: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // 获取命令白名单
    public List<String> getCommandWhitelist() {
        return getConfig().getStringList("command-whitelist");
    }

    // 检查功能是否启用
    public boolean isFeatureEnabled(String feature) {
        return getConfig().getBoolean("settings." + feature, true);
    }

    // 检查是否开启调试模式
    public boolean isDebugMode() {
        return getConfig().getBoolean("settings.debug-mode", false);
    }
}
