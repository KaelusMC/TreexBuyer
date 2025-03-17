package me.jetby.treexbuyer.autoBuy;

import me.jetby.treexbuyer.Main;

import java.util.*;

public class AutoBuy {

    private static Map<UUID, List<String>> autoBuyPlayers = new HashMap<>();


    public static void checkPlayer(UUID uuid) {
        if (Main.getInstance().getDatabaseManager().recordExists(uuid.toString())) {
            // Загружаем данные из базы данных, если игрок существует
            autoBuyPlayers.put(uuid, Main.getInstance().getDatabaseManager().getPlayerData(uuid.toString()));
            System.out.println("Данные игрока загружены из базы данных.");
        } else {
            // Если игрок не найден, инициализируем пустой список
            autoBuyPlayers.put(uuid, new ArrayList<>());
            System.out.println("Игрок не найден в базе данных. Инициализирован пустой список.");
        }
    }

    public static void toggleAutoBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyPlayers.get(uuid);
        if (autoBuyList == null) {
            autoBuyList = new ArrayList<>();
            autoBuyPlayers.put(uuid, autoBuyList);
        }

        if (autoBuyList.contains(item)) {
            // Если предмет уже есть в списке, удаляем его
            autoBuyList.remove(item);
            System.out.println("Предмет удален из списка автоскупки: " + item);
        } else {
            // Если предмета нет в списке, добавляем его
            autoBuyList.add(item);
            System.out.println("Предмет добавлен в список автоскупки: " + item);
        }

        // Сохраняем изменения в базу данных
        Main.getInstance().getDatabaseManager().addOrUpdatePlayerData(uuid.toString(), autoBuyList);
    }

    public static List<String> getAutoBuyPlayers(UUID uuid) {
        return autoBuyPlayers.getOrDefault(uuid, new ArrayList<>());
    }

    public static Map<UUID, List<String>> getAutoBuyPlayersMap() {
        return autoBuyPlayers;
    }
}