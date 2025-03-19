package me.jetby.treexbuyer.configurations;

import me.jetby.treexbuyer.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PriseItemLoader {

    public static Map<String, Double> loadItemValuesFromFile(File file) {
        Map<String, Double> itemValues = new HashMap<>();
        if (!file.exists()) {
            Main.getInstance().getLogger().warning("Файл " + file.getName() + " не найден!");
            return itemValues;
        }

        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);


        for (String key : fileConfig.getKeys(false)) {
            double value = fileConfig.getDouble(key);
            itemValues.put(key, value);
        }

        return itemValues;
    }
}
