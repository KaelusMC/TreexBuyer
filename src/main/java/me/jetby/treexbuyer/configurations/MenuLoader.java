package me.jetby.treexbuyer.configurations;

import me.jetby.treexbuyer.menu.Menu;
import me.jetby.treexbuyer.menu.MenuButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class MenuLoader {

    private static Map<String, Menu> listMenu = new HashMap<>();

    public static void loadMenus(FileConfiguration config, File dataFolder) {

        for (String menuId : config.getConfigurationSection("menu").getKeys(false)) {
            String filePath = config.getString("menu." + menuId + ".path");
            File menuFile = new File(dataFolder, filePath);

            if (menuFile.exists()) {
                loadMenuFromFile(menuId, menuFile);
            } else {
                System.out.println("Menu file not found: " + menuFile.getPath());
            }
        }
    }

    private static void loadMenuFromFile(String menuId, File menuFile) {

        FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(menuFile);



        String titleMenu = menuConfig.getString("titleMenu");
        int size = menuConfig.getInt("size");
        List<String> commandOpenMenu = menuConfig.getStringList("commandOpenMenu");
        String permissionOpenMenu = menuConfig.getString("permissionOpenMenu");


        List<MenuButton> buttons = new ArrayList<>();
        if (menuConfig.contains("menuItems")) {
            for (String buttonKey : menuConfig.getConfigurationSection("menuItems").getKeys(false)) {
                String materialName = menuConfig.getString("menuItems." + buttonKey + ".material");
                Material material = Material.getMaterial(materialName);

                Object slotString = menuConfig.get("menuItems." + buttonKey + ".slot");
                List<Integer> slots = parseSlots(slotString); // Используем parseSlots для обработки слотов


                String titleButton = menuConfig.getString("menuItems." + buttonKey + ".displayButton");
                List<String> loreButton = menuConfig.getStringList("menuItems." + buttonKey + ".loreButton");
                List<String> commands = menuConfig.getStringList("menuItems." + buttonKey + ".command");

                boolean hide_enchantments = menuConfig.getBoolean("menuItems." + buttonKey + ".hide_enchantments", false);
                boolean hide_attributes = menuConfig.getBoolean("menuItems." + buttonKey + ".hide_attributes", false);
                boolean enchanted = menuConfig.getBoolean("menuItems." + buttonKey + ".enchanted", false);



                for (int slot : slots) {
                    MenuButton menuButton = new MenuButton(slot, titleButton, loreButton, material, commands, hide_enchantments, hide_attributes, enchanted);
                    buttons.add(menuButton);
                }

            }
        }


        Menu menu = new Menu(menuId, titleMenu, size, commandOpenMenu, permissionOpenMenu, buttons);


        listMenu.put(menuId, menu);
    }

    public static List<Integer> parseSlots(Object slotObject) {
        List<Integer> slots = new ArrayList<>();

        if (slotObject instanceof Integer) {
            slots.add((Integer) slotObject); // Одиночное число
        } else if (slotObject instanceof String) {
            String slotString = ((String) slotObject).trim();
            slots.addAll(parseSlotString(slotString));
        } else if (slotObject instanceof List<?>) {
            for (Object obj : (List<?>) slotObject) {
                if (obj instanceof Integer) {
                    slots.add((Integer) obj);
                } else if (obj instanceof String) {
                    slots.addAll(parseSlotString((String) obj));
                }
            }
        } else {
            Bukkit.getLogger().warning("Неизвестный формат слотов: " + slotObject);
        }

        return slots;
    }

    private static List<Integer> parseSlotString(String slotString) {
        List<Integer> slots = new ArrayList<>();
        if (slotString.contains("-")) {
            try {
                String[] range = slotString.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Ошибка парсинга диапазона слотов: " + slotString);
            }
        } else {
            try {
                slots.add(Integer.parseInt(slotString));
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Ошибка парсинга одиночного слота: " + slotString);
            }
        }
        return slots;
    }


    public static Map<String, Menu> getListMenu() {
        return listMenu;
    }
}
