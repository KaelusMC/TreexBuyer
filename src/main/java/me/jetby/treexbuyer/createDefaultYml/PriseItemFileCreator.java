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
            writer.write("NETHERITE_INGOT: 10000\n");
            writer.write("NETHERITE_SCRAP: 2000\n");
            writer.write("EMERALD: 1500\n");
            writer.write("DIAMOND: 500\n");
            writer.write("GOLD_INGOT: 200\n");
            writer.write("IRON_INGOT: 80\n");
            writer.write("REDSTONE: 15\n");
            writer.write("LAPIS_LAZULI: 5\n");
            writer.write("COAL: 8\n");
            writer.write("QUARTZ: 10\n");
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать файл priseItem.yml");
            e.printStackTrace();
        }
    }
}
