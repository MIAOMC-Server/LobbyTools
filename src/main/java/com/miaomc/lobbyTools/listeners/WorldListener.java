package com.miaomc.lobbyTools.listeners;

import com.miaomc.lobbyTools.LobbyTools;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldListener implements Listener {
    private final LobbyTools plugin;

    public WorldListener(LobbyTools plugin) {
        this.plugin = plugin;
    }

    // 阻止玩家破坏任何方块
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.isFeatureEnabled("prevent-block-break")) return;

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE || !player.hasPermission("miaomc.lobbytools.bypass")) {
            event.setCancelled(true);
        }
    }

    // 阻止玩家放置任何方块
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.isFeatureEnabled("prevent-block-place")) return;

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE || !player.hasPermission("miaomc.lobbytools.bypass")) {
            event.setCancelled(true);
        }
    }

    // 处理玩家交互事件（收获作物、使用箱子、门等）
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // 如果没有点击方块或玩家有权限跳过限制，则直接返回
        if (clickedBlock == null || player.hasPermission("miaomc.lobbytools.bypass")) {
            return;
        }

        // 阻止踩踏农田
        if (plugin.isFeatureEnabled("prevent-farmland-trampling")) {
            if (event.getAction() == Action.PHYSICAL) {
                Material blockType = clickedBlock.getType();
                if (blockType == Material.FARMLAND) {
                    event.setCancelled(true);
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("阻止玩家 " + player.getName() + " 踩踏农田");
                    }
                    return;
                }
            }
        }

        // 阻止踩压力板
        if (plugin.isFeatureEnabled("prevent-pressure-plate")) {
            if (event.getAction() == Action.PHYSICAL) {
                Material blockType = clickedBlock.getType();
                String materialName = blockType.name();
                if (materialName.contains("PRESSURE_PLATE")) {
                    event.setCancelled(true);
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("阻止玩家 " + player.getName() + " 踩踏压力板: " + blockType.name());
                    }
                    return;
                }
            }
        }

        // 处理收获作物的互动
        if (plugin.isFeatureEnabled("prevent-harvest")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Material blockType = clickedBlock.getType();
                if (blockType == Material.WHEAT ||
                        blockType == Material.POTATOES ||
                        blockType == Material.CARROTS) {
                    event.setCancelled(true);
                    return; // 已经取消了事件，无需继续处理
                }
            }
        }

        // 处理方块交互（箱子、门、信标等）
        if (plugin.isFeatureEnabled("prevent-block-interact")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Material blockType = clickedBlock.getType();

                // 箱子类
                if (blockType == Material.CHEST ||
                        blockType == Material.TRAPPED_CHEST ||
                        blockType == Material.ENDER_CHEST) {
                    event.setCancelled(true);
                    return;
                }

                // 门类和活版门类
                String materialName = blockType.name();
                if (materialName.endsWith("_DOOR") ||
                        materialName.endsWith("_FENCE_GATE") ||
                        materialName.endsWith("_TRAPDOOR")) {
                    event.setCancelled(true);
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("阻止玩家 " + player.getName() + " 与门或活版门交互: " + blockType.name());
                    }
                    return;
                }

                // 红石类交互方块
                if (blockType == Material.LEVER ||
                        materialName.endsWith("_BUTTON") ||
                        blockType == Material.COMPARATOR ||
                        blockType == Material.REPEATER ||
                        blockType == Material.DAYLIGHT_DETECTOR) {
                    event.setCancelled(true);
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("阻止玩家 " + player.getName() + " 与红石方块交互: " + blockType.name());
                    }
                    return;
                }

                // 其他可交互的方块
                if (blockType == Material.BEACON ||
                        blockType == Material.FURNACE ||
                        blockType == Material.BREWING_STAND ||
                        blockType == Material.ENCHANTING_TABLE ||
                        materialName.contains("ANVIL") ||
                        blockType == Material.CRAFTING_TABLE ||
                        blockType == Material.HOPPER ||
                        blockType == Material.DROPPER ||
                        blockType == Material.DISPENSER) {
                    event.setCancelled(true);
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("阻止玩家 " + player.getName() + " 与方块交互: " + blockType.name());
                    }
                }
            }
        }
    }
}
