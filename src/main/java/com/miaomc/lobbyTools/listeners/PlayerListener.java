package com.miaomc.lobbyTools.listeners;

import com.miaomc.lobbyTools.LobbyTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class PlayerListener implements Listener {
    private final LobbyTools plugin;
    private final Random random = new Random();

    public PlayerListener(LobbyTools plugin) {
        this.plugin = plugin;
    }

    // 当玩家加入服务器时传送到出生点
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 检查是否启用了出生点传送功能
        if (plugin.isFeatureEnabled("spawn-teleport")) {
            if (plugin.getSpawnLocation() != null) {
                player.teleport(plugin.getSpawnLocation());
            } else {
                plugin.getLogger().warning("无法传送玩家 " + player.getName() + " 到出生点，因为出生点尚未加载。");
            }
        }

        // 检查是否启用了显示Title功能
        if (plugin.isFeatureEnabled("show-join-title")) {
            showRandomTitleToPlayer(player);
        }
    }

    // 从配置中随机选择并显示标题给玩家，使用MiniMessage格式
    private void showRandomTitleToPlayer(Player player) {
        ConfigurationSection titleSection = plugin.getConfig().getConfigurationSection("title");
        if (titleSection == null) return;

        // 获取主标题和副标题列表
        List<String> mainTitles = titleSection.getStringList("main");
        List<String> subTitles = titleSection.getStringList("sub");

        if (mainTitles.isEmpty()) {
            // 如果列表为空，使用默认值
            mainTitles.add("<gradient:#7FFFD4:#40E0D0><bold>MIAOMC Network</bold></gradient>");
        }
        if (subTitles.isEmpty()) {
            // 如果列表为空，使用默认值
            subTitles.add("<yellow>祝您游戏愉快!</yellow>");
        }

        // 随机选择一条主标题和副标题
        String mainTitleText = mainTitles.get(random.nextInt(mainTitles.size()));
        String subTitleText = subTitles.get(random.nextInt(subTitles.size()));

        // 使用MiniMessage解析文本
        Component mainTitle = plugin.parseMiniMessage(mainTitleText);
        Component subTitle = plugin.parseMiniMessage(subTitleText);

        // 获取标题显示时间配置
        int fadeIn = titleSection.getInt("fade-in", 10);
        int stay = titleSection.getInt("stay", 70);
        int fadeOut = titleSection.getInt("fade-out", 20);

        // 创建Title对象
        Title title = Title.title(
                mainTitle,
                subTitle,
                Times.times(
                        Duration.ofMillis(fadeIn * 50), // tick转换为毫秒
                        Duration.ofMillis(stay * 50),
                        Duration.ofMillis(fadeOut * 50)
                )
        );

        // 使用Adventure API显示Title
        plugin.adventure().player(player).showTitle(title);
    }

    // 检测玩家是否掉入虚空，如果是则传送回出生点
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.isFeatureEnabled("void-teleport")) return;

        Player player = event.getPlayer();
        // 检查玩家是否跌落虚空 (通常Y坐标小于0)
        if (player.getLocation().getY() < 0) {
            if (plugin.getSpawnLocation() != null) {
                player.teleport(plugin.getSpawnLocation());
            } else {
                // 如果出生点未加载，传送到玩家所在世界的出生点
                player.teleport(player.getWorld().getSpawnLocation());
                plugin.getLogger().warning("无法传送玩家 " + player.getName() + " 到插件出生点，已使用世界出生点作为替代。");
            }
        }
    }

    // 阻止玩家捡起任何掉落的物品
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!plugin.isFeatureEnabled("prevent-item-pickup")) return;

        Player player = event.getPlayer();
        event.setCancelled(true);
    }
}
