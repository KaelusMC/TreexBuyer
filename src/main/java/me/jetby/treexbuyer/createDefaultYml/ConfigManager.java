package me.jetby.treexbuyer.createDefaultYml;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private final File menuFolder;

    public ConfigManager(JavaPlugin plugin) {
        this.menuFolder = new File(plugin.getDataFolder(), "Menu");

        // Создаем папку Menu, если её нет
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
        }

        // Создаем конфигурационные файлы, если их нет
        createConfig("seller.yml",
                "titleMenu: Скупщик\n" +
                        "commandOpenMenu:\n" +
                        "- 'seller'\n" +
                        "- 'sell'\n" +
                        "- 'openseller'\n" +
                        "permissionOpenMenu: none\n" +
                        "size: 54\n" +
                        "menuItems:\n" +
                        "  buttonSell:\n" +
                        "    material: AIR\n" +
                        "    slot: 0-40\n" +
                        "    displayButton: ' '\n" +
                        "    loreButton: []\n" +
                        "    command:\n" +
                        "    - '[sell_zone]'\n" +
                        "  buttonSell2:\n" +
                        "    material: DIRT\n" +
                        "    slot: 49\n" +
                        "    displayButton: ' '\n" +
                        "    loreButton:\n" +
                        "    - '&fНажмите чтобы сдать все предметы'\n" +
                        "    - '&fПредметов на сумму: %seller_pay%'\n" +
                        "    command:\n" +
                        "    - '[sell_all]'\n" +
                        "  categor:\n" +
                        "    material: APPLE\n" +
                        "    slot: 45\n" +
                        "    displayButton: ' '\n" +
                        "    loreButton:\n" +
                        "    - ' '\n" +
                        "    command:\n" +
                        "    - '[open_menu] category'");

        createConfig("mine.yml",
                "titleMenu: \"Ресурсы с шахты\"\n" +
                        "commandOpenMenu:\n" +
                        "- 'mine'\n" +
                        "permissionOpenMenu: none\n" +
                        "size: 54");

        createConfig("mobs.yml",
                "titleMenu: \"Ресурсы с мобов\"\n" +
                        "commandOpenMenu:\n" +
                        "- 'openmob'\n" +
                        "permissionOpenMenu: none\n" +
                        "size: 54");
    }

    private void createConfig(String fileName, String defaultContent) {
        File file = new File(menuFolder, fileName);
        if (!file.exists()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(defaultContent);
                Bukkit.getLogger().info("Создан конфигурационный файл: " + fileName);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Не удалось создать конфигурационный файл: " + fileName);
                e.printStackTrace();
            }
        }
    }
}
