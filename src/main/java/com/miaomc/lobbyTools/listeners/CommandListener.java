package com.miaomc.lobbyTools.listeners;

import com.miaomc.lobbyTools.LobbyTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener {
    private final LobbyTools plugin;

    public CommandListener(LobbyTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.isFeatureEnabled("prevent-commands")) return;

        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase().split(" ")[0];

        // 如果玩家有权限或命令在白名单中，则允许执行
        if (player.hasPermission("miaomc.lobbytools.command") || isWhitelistedCommand(command)) {
            return;
        }

        // 否则取消命令执行
        event.setCancelled(true);
        player.sendMessage(plugin.getMessage("no-command-permission"));
    }

    // 处理TAB补全事件
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        // 检查该功能是否启用
        if (!plugin.isFeatureEnabled("prevent-tab-completion")) return;

        // 确保发送者是玩家
        if (!(event.getSender() instanceof Player)) return;

        Player player = (Player) event.getSender();

        // 如果玩家有绕过权限，允许所有TAB补全
        if (player.hasPermission("miaomc.lobbytools.command")) {
            return;
        }

        // 获取当前输入的命令字符串
        String buffer = event.getBuffer().toLowerCase();

        // 确保这是一个命令（以/开头）
        if (!buffer.startsWith("/")) return;

        // 提取命令名称（不包含参数）
        String[] parts = buffer.split(" ");
        String baseCommand = parts[0]; // 例如 "/plugin:command" 或 "/command"

        // 检查是否是完整的命令名称或命令的部分
        if (parts.length <= 1) {
            // 用户只输入了命令名称（没有空格和参数）
            List<String> whitelistedCommands = plugin.getCommandWhitelist();
            List<String> allowedCompletions = new ArrayList<>();

            // 只允许补全白名单中的命令
            for (String completion : event.getCompletions()) {
                // 构建完整命令进行检查
                String fullCommand = baseCommand + completion;

                // 检查这个补全结果是否在白名单中或是白名单命令的前缀
                boolean allowed = false;
                for (String whitelistedCmd : whitelistedCommands) {
                    if (whitelistedCmd.toLowerCase().equals(fullCommand) ||
                            whitelistedCmd.toLowerCase().startsWith(fullCommand)) {
                        allowed = true;
                        break;
                    }
                }

                if (allowed) {
                    allowedCompletions.add(completion);
                }
            }

            // 设置过滤后的补全结果
            event.setCompletions(allowedCompletions);
        } else {
            // 用户输入了命令及其参数
            // 检查基础命令是否在白名单中
            if (!isWhitelistedCommand(baseCommand)) {
                // 如果命令不在白名单中，清空补全结果
                event.setCompletions(new ArrayList<>());
            }
        }
    }

    private boolean isWhitelistedCommand(String command) {
        List<String> whitelistedCommands = plugin.getCommandWhitelist();
        return whitelistedCommands.contains(command);
    }
}
