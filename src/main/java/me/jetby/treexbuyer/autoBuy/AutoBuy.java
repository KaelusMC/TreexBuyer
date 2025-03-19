package me.jetby.treexbuyer.autoBuy;

import me.jetby.treexbuyer.Main;
import org.bukkit.Bukkit;

import java.util.*;

import static me.jetby.treexbuyer.configurations.Config.CFG;
import static org.bukkit.Bukkit.getLogger;

public class AutoBuy {

    private static Map<UUID, List<String>> autoBuyPlayers = new HashMap<>();


    public static void checkPlayer(UUID uuid) {
        if (Main.getInstance().getDatabaseManager().recordExists(uuid.toString())) {
            autoBuyPlayers.put(uuid, Main.getInstance().getDatabaseManager().getPlayerData(uuid.toString()));
            if (CFG().getBoolean("logger")) {
                getLogger().info("Данные игрока загружены из базы данных.");
            }
        } else {
            autoBuyPlayers.put(uuid, new ArrayList<>());
            if (CFG().getBoolean("logger")) {
                getLogger().info("Игрок не найден в базе данных. Инициализирован пустой список.");
            }
        }
    }

    public static void toggleAutoBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyPlayers.get(uuid);
        if (autoBuyList == null) {
            autoBuyList = new ArrayList<>();
            autoBuyPlayers.put(uuid, autoBuyList);
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

    public static List<String> getAutoBuyPlayers(UUID uuid) {
        return autoBuyPlayers.getOrDefault(uuid, new ArrayList<>());
    }

    public static Map<UUID, List<String>> getAutoBuyPlayersMap() {
        return autoBuyPlayers;
    }
}