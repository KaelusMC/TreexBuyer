package me.jetby.treexbuyer.dataBase;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.jetby.treexbuyer.configurations.Config.CFG;
import static org.bukkit.Bukkit.getLogger;

public class DatabaseManager {
    private static final String DATABASE_NAME = "data.db";
    private static final String TABLE_NAME = "player_data";
    private Connection connection;

    public DatabaseManager() {
    }

    public void initDatabase() {
        try {
            File dbFile = new File("plugins/TreexBuyer/data.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTable();

            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] База данных создана в: " + dbFile.getAbsolutePath());
            }


        } catch (SQLException e) {
            e.printStackTrace();

            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Ошибка подключения к базе данных: " + e.getMessage());
            }
        }
    }


    private void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_data (uuid TEXT PRIMARY KEY, data TEXT);";
        String createScoresTableSQL = "CREATE TABLE IF NOT EXISTS player_scores (uuid TEXT PRIMARY KEY, scores INTEGER);";

        try (PreparedStatement statement = connection.prepareStatement(createTableSQL);
             PreparedStatement scoresStatement = connection.prepareStatement(createScoresTableSQL)) {
            statement.execute();
            scoresStatement.execute();
            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Таблицы player_data и player_scores успешно созданы или уже существуют.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Ошибка при создании таблиц: " + e.getMessage());
            }
        }
    }
    public void addOrUpdatePlayerScores(String uuid, double scores) {
        String upsertSQL = "INSERT INTO player_scores (uuid, scores) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET scores = ?;";

        try (PreparedStatement statement = connection.prepareStatement(upsertSQL)) {
            statement.setString(1, uuid);
            statement.setDouble(2, scores);
            statement.setDouble(3, scores);
            statement.executeUpdate();

            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Очки для UUID=" + uuid + " были успешно добавлены/обновлены.");
                getLogger().info("[TreexBuyer] Бустер для UUID=" + uuid + " теперь: " + scores);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Ошибка при добавлении или обновлении очков: " + e.getMessage());
            }
        }
    }

    public double getPlayerScores(String uuid) {
        String selectSQL = "SELECT scores FROM player_scores WHERE uuid = ?;";

        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("scores");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Ошибка при получении очков: " + e.getMessage());
            }
        }

        return 0; // Если очков нет, возвращаем 0
    }

    public void addOrUpdatePlayerData(String uuid, List<String> materials) {
        String data = String.join(",", materials);
        String upsertSQL = "INSERT INTO player_data (uuid, data) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET data = ?;";

        try (PreparedStatement statement = connection.prepareStatement(upsertSQL)) {
            statement.setString(1, uuid);
            statement.setString(2, data);
            statement.setString(3, data);
            statement.executeUpdate();

            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Данные для UUID=" + uuid + " были успешно добавлены или обновлены.");
            }

        } catch (SQLException e) {
            e.printStackTrace();

            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Ошибка при добавлении или обновлении данных: " + e.getMessage());
            }
        }

    }

    public boolean recordExists(String uuid) {
        String selectSQL = "SELECT 1 FROM player_data WHERE uuid = ?;";

        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();

            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Ошибка при проверке записи: " + e.getMessage());
            }
            return false;
        }
    }

    public List<String> getPlayerData(String uuid) {
        String selectSQL = "SELECT data FROM player_data WHERE uuid = ?;";

        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String data = resultSet.getString("data");
                return new ArrayList(Arrays.asList(data.split(",")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (CFG().getBoolean("logger")) {
                getLogger().info("[TreexBuyer] Ошибка при получении данных: " + e.getMessage());
            }
        }

        return new ArrayList();
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();

                if (CFG().getBoolean("logger")) {
                    getLogger().info("[TreexBuyer] Подключение к базе данных закрыто.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Connection getConnection() {
        return connection;
    }
}
