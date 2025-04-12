package me.jetby.treexbuyer.autoBuy;

import me.jetby.treexbuyer.Main;
import org.bukkit.Bukkit;

import java.util.*;

import static me.jetby.treexbuyer.configurations.Config.CFG;
import static org.bukkit.Bukkit.getLogger;

public class AutoBuy {

    private static Map<UUID, List<String>> autoBuyItemsPlayers = new HashMap<>();
    private static HashMap<UUID, Boolean> autoBuyStatus = new HashMap<>();


    public static void checkPlayer(UUID uuid) {
        if (Main.getInstance().getDatabaseManager().recordExists(uuid.toString())) {
            autoBuyItemsPlayers.put(uuid, Main.getInstance().getDatabaseManager().getPlayerData(uuid.toString()));
            if (CFG().getBoolean("logger")) {
                getLogger().info("Данные игрока загружены из базы данных.");
            }
        } else {
            autoBuyItemsPlayers.put(uuid, new ArrayList<>());
            if (CFG().getBoolean("logger")) {
                getLogger().info("Игрок не найден в базе данных. Инициализирован пустой список.");
            }
        }
    }
    public static boolean getAutoBuyStatus(UUID uuid) {
        return Main.getInstance().getDatabaseManager().getAutoBuyStatus(uuid.toString());
    }

    public static void setAutoBuyStatus(UUID uuid, boolean status) {
        autoBuyStatus.put(uuid, status);
        Main.getInstance().getDatabaseManager().setAutoBuyStatus(uuid.toString(), status);
    }


    public static void enableAutBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyItemsPlayers.get(uuid);
        if (autoBuyList.isEmpty()) {
            autoBuyList = new ArrayList<>();
            autoBuyItemsPlayers.put(uuid, autoBuyList);
        }

        if (!autoBuyList.contains(item)) {
            autoBuyList.add(item);
            if (CFG().getBoolean("logger")) {
                getLogger().info("Предмет добавлен в список автоскупки: " + item);
            }
        }

        Main.getInstance().getDatabaseManager().addOrUpdatePlayerData(uuid.toString(), autoBuyList);
    }
    public static void disableAutBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyItemsPlayers.get(uuid);
        if (autoBuyList.isEmpty()) {
            autoBuyList = new ArrayList<>();
            autoBuyItemsPlayers.put(uuid, autoBuyList);
        }

        if (autoBuyList.contains(item)) {
            autoBuyList.remove(item);
            if (CFG().getBoolean("logger")) {
                getLogger().info("Предмет удален из списка автоскупки: " + item);
            }
        }

        Main.getInstance().getDatabaseManager().addOrUpdatePlayerData(uuid.toString(), autoBuyList);
    }
    public static void toggleAutoBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyItemsPlayers.get(uuid);
        if (autoBuyList.isEmpty()) {
            autoBuyList = new ArrayList<>();
            autoBuyItemsPlayers.put(uuid, autoBuyList);
        }

        if (autoBuyList.contains(item)) {
            autoBuyList.remove(item);
            if (CFG().getBoolean("logger")) {
                getLogger().info("Предмет удален из списка автоскупки: " + item);
            }
        } else {
            autoBuyList.add(item);
            if (CFG().getBoolean("logger")) {
                getLogger().info("Предмет добавлен в список автоскупки: " + item);
            }
        }

        Main.getInstance().getDatabaseManager().addOrUpdatePlayerData(uuid.toString(), autoBuyList);
    }

    public static List<String> getAutoBuyItems(UUID uuid) {
        return autoBuyItemsPlayers.getOrDefault(uuid, new ArrayList<>());
    }


    public static Map<UUID, List<String>> getAutoBuyItemsMap() {
        return autoBuyItemsPlayers;
    }
}