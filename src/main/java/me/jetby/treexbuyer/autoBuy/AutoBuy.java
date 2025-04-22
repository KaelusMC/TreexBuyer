package me.jetby.treexbuyer.autoBuy;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.dataBase.DatabaseUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.*;

import static me.jetby.treexbuyer.boost.CoefficientManager.getCachedScores;
import static me.jetby.treexbuyer.configurations.Config.CFG;
import static org.bukkit.Bukkit.getLogger;

public class AutoBuy {

    private static final Map<UUID, List<String>> autoBuyItemsPlayers = new HashMap<>();
    private static final Map<UUID, Boolean> autoBuyStatus = new HashMap<>();


    public static void loadPlayerAutoBuyAsync(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            List<String> data = DatabaseUtils.get("autobuy", uuid, "items", (rs) -> {
                try {
                    String str = rs.getString("items");
                    return str != null ? Arrays.asList(str.split(",")) : new ArrayList<>();
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            });

            Boolean status = DatabaseUtils.get("autobuy", uuid, "status", rs -> {
                try {
                    return rs.getBoolean("status");
                } catch (SQLException e) {
                    return false;
                }
            });

            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                autoBuyItemsPlayers.put(uuid, data != null ? data : new ArrayList<>());
                autoBuyStatus.put(uuid, status != null && status);
                log("Данные игрока " + uuid + " загружены из БД.");
            });
        });
    }

    public static boolean getAutoBuyStatus(UUID uuid) {

        Boolean status = DatabaseUtils.get("autobuy", uuid, "status", rs -> {
            try {
                return rs.getBoolean("status");
            } catch (SQLException e) {
                return false;
            }
        });
        return status != null && status;
    }

    public static void setAutoBuyStatus(UUID uuid, boolean status) {
        autoBuyStatus.put(uuid, status);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            DatabaseUtils.set("autobuy", uuid, "status", status);
        });
    }

    public static void enableAutBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyItemsPlayers.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (!autoBuyList.contains(item)) {
            autoBuyList.add(item);
            log("Предмет добавлен в список автоскупки: " + item);
            updatePlayerItems(uuid, autoBuyList);
        }
    }

    public static void disableAutBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyItemsPlayers.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (autoBuyList.contains(item)) {
            autoBuyList.remove(item);
            log("Предмет удален из списка автоскупки: " + item);
            updatePlayerItems(uuid, autoBuyList);
        }
    }

    public static void savePlayerScoreAsync(UUID uuid) {
        Double score = getCachedScores().remove(uuid);
        if (score != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("scores", score);
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                DatabaseUtils.insertOrUpdate("players", uuid, data);
            });
        }
    }
    public static void savePlayerScore(UUID uuid) {
        Double score = getCachedScores().remove(uuid);
        if (score != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("scores", score);
                DatabaseUtils.insertOrUpdate("players", uuid, data);
        }
    }


    public static void toggleAutoBuyItem(UUID uuid, String item) {
        List<String> autoBuyList = autoBuyItemsPlayers.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (autoBuyList.contains(item)) {
            autoBuyList.remove(item);
            log("Предмет удален из списка автоскупки: " + item);
        } else {
            autoBuyList.add(item);
            log("Предмет добавлен в список автоскупки: " + item);
        }
        updatePlayerItems(uuid, autoBuyList);
    }

    public static List<String> getAutoBuyItems(UUID uuid) {
        return autoBuyItemsPlayers.computeIfAbsent(uuid, k -> new ArrayList<>());
    }


    public static Map<UUID, List<String>> getAutoBuyItemsMap() {
        return autoBuyItemsPlayers;
    }

    private static void updatePlayerItems(UUID uuid, List<String> items) {
        String joined = String.join(",", items);
        Map<String, Object> data = new HashMap<>();
        data.put("items", joined);

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            DatabaseUtils.insertOrUpdate("autobuy", uuid, data);
        });
    }

    private static void log(String msg) {
        if (CFG().getBoolean("logger")) {
            getLogger().info(msg);
        }
    }
}
