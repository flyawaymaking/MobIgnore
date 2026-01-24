package com.flyaway.mobignore;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class MobIgnore extends JavaPlugin implements Listener, TabCompleter {

    private ConfigManager configManager;
    private DataManager dataManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);

        hookPlaceholderAPI();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("mobignore").setTabCompleter(this);

        getLogger().info("MobIgnore enabled!");
    }

    private void hookPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MobIgnorePlaceholder(this).register();
            getLogger().info("PlaceholderAPI found, placeholders enabled");
        }
    }

    @Override
    public void onDisable() {
        if (dataManager.isDataLoaded()) dataManager.saveData();
        getLogger().info("MobIgnore disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "player-only");
            return true;
        }
        if (args.length == 0) {

            if (!player.hasPermission("mobignore.use")) {
                sendMessage(player, "no-permission");
                return true;
            }

            UUID playerId = player.getUniqueId();

            if (dataManager.isIgnoredPlayer(playerId)) {
                dataManager.removeIgnoredPlayer(playerId);
                sendMessage(player, "enabled-mobs-attack");
            } else {
                dataManager.addIgnoredPlayer(playerId);
                clearTargetsForPlayer(player);
                sendMessage(player, "ignore-enabled");
            }
        } else if (args[0].equals("reload")) {
            if (!player.hasPermission("mobignore.reload")) {
                sendMessage(player, "no-permission");
                return true;
            }

            configManager.reload();
            sendMessage(player, "reloaded");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (!command.getName().equalsIgnoreCase("mobignore")) {
            return List.of();
        }

        if (args.length == 1) {
            if (sender.hasPermission("mobignore.reload")) {
                if ("reload".startsWith(args[0].toLowerCase())) {
                    return List.of("reload");
                }
            }
        }

        return List.of();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof ExperienceOrb) return;

        if (event.getTarget() instanceof Player player) {
            if (dataManager.isIgnoredPlayer(player.getUniqueId())) {
                if (event.getEntityType() != EntityType.PLAYER) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (dataManager.isIgnoredPlayer(playerId) && !player.hasPermission("mobignore.use")) {
            dataManager.removeIgnoredPlayer(player.getUniqueId());
        }
    }

    private void clearTargetsForPlayer(Player player) {
        int targetClearRadius = configManager.getTargetClearRadius();
        UUID playerId = player.getUniqueId();

        for (Entity entity : player.getNearbyEntities(targetClearRadius, targetClearRadius, targetClearRadius)) {
            if (entity instanceof Mob mob) {
                if (mob.getTarget() != null && mob.getTarget().getUniqueId().equals(playerId)) {
                    mob.setTarget(null);
                }
            }
        }
    }

    private void sendMessage(CommandSender sender, String path) {
        String raw = configManager.getConfig().getString("messages." + path, "<red>Not found message: " + path);
        if (raw.isEmpty()) return;
        sender.sendMessage(miniMessage.deserialize(raw));
    }

    public String getStatusText(boolean ignored) {
        String path = ignored ? "placeholders.enabled" : "placeholders.disabled";
        return configManager.getConfig().getString(path, "&c&lNot configured");
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
