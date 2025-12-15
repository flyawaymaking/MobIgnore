package com.flyaway.mobignore;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
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
    private MiniMessage mm;

    private int targetClearRadius;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mm = MiniMessage.miniMessage();

        targetClearRadius = getConfig().getInt("target-clear-radius", 32);

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MobIgnore включён!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobIgnore выключен!");
    }

    private void msg(CommandSender sender, String path) {
        String raw = getConfig().getString("messages." + path, "<red>Not found message: " + path);
        sender.sendMessage(mm.deserialize(raw));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("mobignore.use")) {
            msg(player, "no-permission");
            return true;
        }

        if (ignoredPlayers.contains(player.getUniqueId())) {
            ignoredPlayers.remove(player.getUniqueId());
            msg(player, "enabled-mobs-attack");
        } else {
            ignoredPlayers.add(player.getUniqueId());

            clearTargetsForPlayer(player);

            msg(player, "ignore-enabled");
        }

        return true;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof ExperienceOrb) return;

        if (event.getTarget() instanceof Player player) {
            if (ignoredPlayers.contains(player.getUniqueId())) {
                if (event.getEntityType() != EntityType.PLAYER) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void clearTargetsForPlayer(Player player) {
        for (Entity entity : player.getNearbyEntities(targetClearRadius, targetClearRadius, targetClearRadius)) {
            if (entity instanceof Mob mob) {
                if (mob.getTarget() != null && mob.getTarget().getUniqueId().equals(player.getUniqueId())) {
                    mob.setTarget(null);
                }
            }
        }
    }
}
