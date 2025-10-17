package com.flyaway.mobignore;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class MobIgnore extends JavaPlugin implements Listener {
    private final HashSet<UUID> ignoredPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MobIgnore включён!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobIgnore выключен!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду можно использовать только в игре!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("mobignore.use")) {
            player.sendMessage("У тебя нет прав использовать эту команду.");
            return true;
        }

        if (ignoredPlayers.contains(player.getUniqueId())) {
            ignoredPlayers.remove(player.getUniqueId());
            player.sendMessage("§eМобы снова могут нападать на тебя.");
        } else {
            ignoredPlayers.add(player.getUniqueId());
            // Сбрасываем все текущие цели на этого игрока
            clearTargetsForPlayer(player);
            player.sendMessage("§aТеперь мобы будут тебя игнорировать!");
        }
        return true;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            if (ignoredPlayers.contains(player.getUniqueId())) {
                // Если игрок в "игноре", то мобы не могут его атаковать
                if (event.getEntityType() != EntityType.PLAYER) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Сбрасывает цели всех мобов, которые нацелены на конкретного игрока
     */
    private void clearTargetsForPlayer(Player player) {
        // Ищем мобов в радиусе 32 блоков от игрока
        for (Entity entity : player.getNearbyEntities(32, 32, 32)) {
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;

                // Проверяем, нацелен ли моб на этого игрока
                if (mob.getTarget() != null && mob.getTarget().getUniqueId().equals(player.getUniqueId())) {
                    mob.setTarget(null);
                }
            }
        }
    }
}
