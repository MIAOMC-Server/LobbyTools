package com.miaomc.lobbyTools.listeners;

import com.miaomc.lobbyTools.LobbyTools;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

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

    // 从配置中随机选择并显示标题给玩家
    private void showRandomTitleToPlayer(Player player) {
        ConfigurationSection titleSection = plugin.getConfig().getConfigurationSection("title");
        if (titleSection == null) return;

        // 获取主标题和副标题列表
        List<String> mainTitles = titleSection.getStringList("main");
        List<String> subTitles = titleSection.getStringList("sub");

        if (mainTitles.isEmpty()) {
            // 如果列表为空，使用默认值
            mainTitles.add("&6欢迎来到服务器!");
        }
        if (subTitles.isEmpty()) {
            // 如果列表为空，使用默认值
            subTitles.add("&e祝您游戏愉快!");
        }

        // 随机选择一条主标题和副标题
        String mainTitle = ChatColor.translateAlternateColorCodes('&',
                mainTitles.get(random.nextInt(mainTitles.size())));
        String subTitle = ChatColor.translateAlternateColorCodes('&',
                subTitles.get(random.nextInt(subTitles.size())));

        // 向玩家显示Title
        // 注意：在您使用的Bukkit API版本中，sendTitle只接受两个参数
        player.sendTitle(mainTitle, subTitle);

        // 记录调试信息
        plugin.getLogger().info("向玩家 " + player.getName() + " 显示标题: " + mainTitle + " - " + subTitle);
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

    // 禁止玩家与门交互
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isFeatureEnabled("prevent-door-interaction")) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                Material material = block.getType();
                // 检查方块类型是否为任何类型的门
                if (material.name().contains("DOOR") ||
                        material.name().contains("GATE") ||
                        material.name().contains("TRAPDOOR") ||
                        material == Material.FENCE_GATE) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
