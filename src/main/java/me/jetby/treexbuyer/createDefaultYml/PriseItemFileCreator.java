package me.jetby.treexbuyer.createDefaultYml;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class PriseItemFileCreator {
    private final JavaPlugin plugin;

    public PriseItemFileCreator(JavaPlugin plugin) {
        this.plugin = plugin;
        File priseItemFile = new File(plugin.getDataFolder(), "priseItem.yml");

        // Проверяем, существует ли файл
        if (!priseItemFile.exists()) {
            createDefaultFile(priseItemFile);
        }
    }

    // Создание файла с начальными данными
    private void createDefaultFile(File file) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("# Обычная стоимость предметов\n");
            writer.write("rotten_flesh: 100\n");
            writer.write("bone: 150\n");
            writer.write("iron_ingot: 200\n");
            writer.write("gold_ingot: 300\n");
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать файл priseItem.yml");
            e.printStackTrace();
        }
    }
}
