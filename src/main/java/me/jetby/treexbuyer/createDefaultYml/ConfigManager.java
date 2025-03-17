package me.jetby.treexbuyer.createDefaultYml;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    public void loadYamlFiles(Plugin plugin) {
        File typesFolder = new File(plugin.getDataFolder(), "Menu");

        if (!typesFolder.exists()) {
            typesFolder.mkdirs();
            createDefaultFiles(plugin, typesFolder);
        }

        File[] yamlFiles = typesFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (yamlFiles != null) {
            for (File yamlFile : yamlFiles) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(yamlFile);
                configs.put(yamlFile.getName(), config); // Сохраняем конфигурацию в Map
            }
        }
    }

    private void createDefaultFiles(Plugin plugin, File typesFolder) {
        String[] defaultFiles = {"mine.yml", "mobs.yml", "seller.yml"};
        for (String fileName : defaultFiles) {
            File file = new File(typesFolder, fileName);
            if (!file.exists()) {
                plugin.saveResource("Menu/" + fileName, false);
            }
        }
    }

    // Поле для хранения всех загруженных конфигураций
    private final Map<String, YamlConfiguration> configs = new HashMap<>();

    // Получение конфигурации по имени файла
    public YamlConfiguration getConfig(String name) {
        return configs.get(name + ".yml");
    }


}
