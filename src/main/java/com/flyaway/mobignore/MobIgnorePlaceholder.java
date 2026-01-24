package com.flyaway.mobignore;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobIgnorePlaceholder extends PlaceholderExpansion {

    private final MobIgnore plugin;
    private final DataManager dataManager;

    public MobIgnorePlaceholder(MobIgnore plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mobignore";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().getFirst();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("status")) {
            boolean ignored = dataManager.isIgnoredPlayer(player.getUniqueId());
            return plugin.getStatusText(ignored);
        }

        return null;
    }
}
