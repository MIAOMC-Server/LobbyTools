package com.miaomc.lobbyTools.listeners;

import com.miaomc.lobbyTools.LobbyTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class DamageListener implements Listener {
    private final LobbyTools plugin;

    public DamageListener(LobbyTools plugin) {
        this.plugin = plugin;
    }

    // 取消所有对玩家造成伤害的事件
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!plugin.isFeatureEnabled("prevent-damage")) return;

        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    // 特别处理PVP事件
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!plugin.isFeatureEnabled("prevent-pvp")) return;

        if (event.getEntity() instanceof Player || event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    // 防止饥饿条下降
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!plugin.isFeatureEnabled("prevent-hunger")) return;

        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            ((Player) event.getEntity()).setFoodLevel(20); // 保持饥饿值满
        }
    }
}
