package com.flyaway.mobignore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class DataManager {
    private final MobIgnore plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final HashSet<UUID> ignoredPlayers = new HashSet<>();
    private DataManager dataManager;
    private boolean dataLoaded = false;

    public DataManager(MobIgnore plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        ignoredPlayers.clear();
        dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            dataConfig = new YamlConfiguration();
            saveData();
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String uuid : dataConfig.getStringList("ignored")) {
            ignoredPlayers.add(UUID.fromString(uuid));
        }
        dataLoaded = true;
    }

    public void saveData() {
        List<String> list = ignoredPlayers.stream()
                .map(UUID::toString)
                .toList();

        dataConfig.set("ignored", list);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить data.yml");
        }
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void addIgnoredPlayer(UUID playerId) {
        ignoredPlayers.add(playerId);
    }

    public void removeIgnoredPlayer(UUID playerId) {
        ignoredPlayers.remove(playerId);
    }

    public boolean isIgnoredPlayer(UUID playerId) {
        return ignoredPlayers.contains(playerId);
    }
}
