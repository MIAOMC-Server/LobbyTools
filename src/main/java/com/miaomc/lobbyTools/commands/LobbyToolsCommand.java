package com.miaomc.lobbyTools.commands;

import com.miaomc.lobbyTools.LobbyTools;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyToolsCommand implements CommandExecutor {
    private final LobbyTools plugin;

    public LobbyToolsCommand(LobbyTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("miaomc.lobbytools.use")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用这个命令!");
            return true;
        }

        // 处理命令
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setspawn":
                return setSpawn(sender);
            case "spawn":
                return teleportToSpawn(sender);
            case "reload":
                return reloadPlugin(sender);
            default:
                showHelp(sender);
                return true;
        }
    }

    private boolean setSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行!");
            return true;
        }

        Player player = (Player) sender;
        plugin.saveSpawnLocation(player.getLocation());
        player.sendMessage(plugin.getMessage("spawn-set"));
        return true;
    }

    private boolean teleportToSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行!");
            return true;
        }

        Player player = (Player) sender;
        player.teleport(plugin.getSpawnLocation());
        player.sendMessage(plugin.getMessage("teleport-to-spawn"));
        return true;
    }

    private boolean reloadPlugin(CommandSender sender) {
        plugin.reloadConfig();
        plugin.loadSpawnLocation();
        sender.sendMessage(plugin.getMessage("config-reloaded"));
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "======== LobbyTools 帮助 ========");
        sender.sendMessage(ChatColor.YELLOW + "/lobbytools setspawn " + ChatColor.WHITE + "- 设置大厅出生点");
        sender.sendMessage(ChatColor.YELLOW + "/lobbytools spawn " + ChatColor.WHITE + "- 传送到大厅出生点");
        sender.sendMessage(ChatColor.YELLOW + "/lobbytools reload " + ChatColor.WHITE + "- 重新加载插件配置");
        sender.sendMessage(ChatColor.YELLOW + "/mlt " + ChatColor.WHITE + "- 可用作 /lobbytools 的简短形式");
    }
}
