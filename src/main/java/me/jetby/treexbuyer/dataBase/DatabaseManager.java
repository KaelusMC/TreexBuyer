package me.jetby.treexbuyer.dataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseManager {
    private static final String DATABASE_NAME = "data.db";
    private static final String TABLE_NAME = "player_data";
    private Connection connection;

    public DatabaseManager() {
    }

    public void initDatabase() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:data.db");
            this.createTable();
            System.out.println("[TreexBuyer] База данных успешно создана");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[TreexBuyer] Ошибка подключения к базе данных: " + e.getMessage());
        }

    }

    private void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_data (uuid TEXT PRIMARY KEY,data TEXT);";

        try (PreparedStatement statement = this.connection.prepareStatement(createTableSQL)) {
            statement.execute();
            System.out.println("[TreexBuyer] Таблица player_data успешно создана или уже существует.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[TreexBuyer] Ошибка при создании таблицы: " + e.getMessage());
        }

    }

    public void addOrUpdatePlayerData(String uuid, List<String> materials) {
        String data = String.join(",", materials);
        String upsertSQL = "INSERT INTO player_data (uuid, data) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET data = ?;";

        try (PreparedStatement statement = this.connection.prepareStatement(upsertSQL)) {
            statement.setString(1, uuid);
            statement.setString(2, data);
            statement.setString(3, data);
            statement.executeUpdate();
            System.out.println("[TreexBuyer] Данные для UUID=" + uuid + " были успешно добавлены или обновлены.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[TreexBuyer] Ошибка при добавлении или обновлении данных: " + e.getMessage());
        }

    }

    public boolean recordExists(String uuid) {
        String selectSQL = "SELECT 1 FROM player_data WHERE uuid = ?;";

        try (PreparedStatement statement = this.connection.prepareStatement(selectSQL)) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[TreexBuyer] Ошибка при проверке записи: " + e.getMessage());
            return false;
        }
    }

    public List<String> getPlayerData(String uuid) {
        String selectSQL = "SELECT data FROM player_data WHERE uuid = ?;";

        try (PreparedStatement statement = this.connection.prepareStatement(selectSQL)) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String data = resultSet.getString("data");
                return new ArrayList(Arrays.asList(data.split(",")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[TreexBuyer] Ошибка при получении данных: " + e.getMessage());
        }

        return new ArrayList();
    }

    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                System.out.println("[TreexBuyer] Подключение к базе данных закрыто.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Connection getConnection() {
        return this.connection;
    }
}
