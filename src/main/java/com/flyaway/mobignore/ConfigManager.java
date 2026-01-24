package com.flyaway.mobignore;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ConfigManager {
    private static final int CURRENT_CONFIG_VERSION = 1;

    private final MobIgnore plugin;
    private FileConfiguration config;
    public int targetClearRadius;

    public ConfigManager(MobIgnore plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        int version = config.getInt("config-version", 0);
        if (version < CURRENT_CONFIG_VERSION) {
            plugin.getLogger().info("config.yml has been updated from " + version + " to " + CURRENT_CONFIG_VERSION + " version.");
            FileConfiguration defaultConfig = loadDefaultConfig();
            mergeSection(defaultConfig, config);
            config.set("config-version", CURRENT_CONFIG_VERSION);
            plugin.saveConfig();
        }

        targetClearRadius = config.getInt("target-clear-radius", 32);
    }

    private FileConfiguration loadDefaultConfig() {
        YamlConfiguration defaultConfig = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8)
        ) {
            defaultConfig.load(reader);
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось загрузить дефолтный config.yml");
        }
        return defaultConfig;
    }

    private void mergeSection(ConfigurationSection source, ConfigurationSection target) {
        for (String key : source.getKeys(false)) {
            Object sourceValue = source.get(key);

            if (source.isConfigurationSection(key)) {
                ConfigurationSection sourceSub = source.getConfigurationSection(key);
                if (sourceSub == null) continue;

                ConfigurationSection targetSub = target.getConfigurationSection(key);
                if (targetSub == null) {
                    targetSub = target.createSection(key);
                }

                mergeSection(sourceSub, targetSub);

            } else {
                if (!target.isSet(key)) {
                    target.set(key, sourceValue);
                }
            }
        }
    }

    public int getTargetClearRadius() {
        return targetClearRadius;
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        targetClearRadius = config.getInt("target-clear-radius", 32);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
