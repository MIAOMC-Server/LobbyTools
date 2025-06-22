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

        // 处理收获作物的互动
        if (plugin.isFeatureEnabled("prevent-harvest")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Material blockType = clickedBlock.getType();
                if (blockType == Material.CROPS ||
                        blockType == Material.POTATO ||
                        blockType == Material.CARROT) {
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

                // 门类
                if (blockType == Material.WOODEN_DOOR ||
                        blockType == Material.IRON_DOOR_BLOCK ||
                        blockType == Material.FENCE_GATE ||
                        blockType == Material.ACACIA_DOOR ||
                        blockType == Material.BIRCH_DOOR ||
                        blockType == Material.DARK_OAK_DOOR ||
                        blockType == Material.JUNGLE_DOOR ||
                        blockType == Material.SPRUCE_DOOR ||
                        blockType == Material.TRAP_DOOR) {
                    event.setCancelled(true);
                    return;
                }

                // 红石类交互方块
                if (blockType == Material.LEVER ||
                        blockType == Material.STONE_BUTTON ||
                        blockType == Material.WOOD_BUTTON ||
                        blockType == Material.REDSTONE_COMPARATOR ||
                        blockType == Material.DIODE ||
                        blockType == Material.DAYLIGHT_DETECTOR) {
                    event.setCancelled(true);
                    return;
                }

                // 其他可交互的方块
                if (blockType == Material.BEACON ||
                        blockType == Material.FURNACE ||
                        blockType == Material.BURNING_FURNACE ||
                        blockType == Material.BREWING_STAND ||
                        blockType == Material.ENCHANTMENT_TABLE ||
                        blockType == Material.ANVIL ||
                        blockType == Material.WORKBENCH ||
                        blockType == Material.HOPPER ||
                        blockType == Material.DROPPER ||
                        blockType == Material.DISPENSER) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
