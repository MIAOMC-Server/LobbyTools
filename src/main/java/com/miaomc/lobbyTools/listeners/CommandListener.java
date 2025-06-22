package com.miaomc.lobbyTools.listeners;

import com.miaomc.lobbyTools.LobbyTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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

    private boolean isWhitelistedCommand(String command) {
        List<String> whitelistedCommands = plugin.getCommandWhitelist();
        return whitelistedCommands.contains(command);
    }
}
