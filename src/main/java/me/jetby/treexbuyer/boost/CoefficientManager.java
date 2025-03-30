package me.jetby.treexbuyer.boost;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.configurations.Config;
import me.jetby.treexbuyer.dataBase.DatabaseManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

import static me.jetby.treexbuyer.configurations.Config.CFG;

public class CoefficientManager {
    private static DatabaseManager databaseManager = Main.getInstance().getDatabaseManager();
    public static final DecimalFormat df = new DecimalFormat("#.##");
    public static double getPlayerCoefficient(Player player) {
        UUID playerId = player.getUniqueId();

        // Базовый коэффициент
        double defaultCoefficient = CFG().getDouble("default-coefficient", 1.0);

        // Получаем очки игрока из базы данных
        double playerScore = databaseManager.getPlayerScores(playerId.toString());

        // Коэффициент за каждые 100 очков
        int scoreStep = CFG().getInt("score-to-multiplier-ratio.scores", 100);
        double coefficientStep = CFG().getDouble("score-to-multiplier-ratio.coefficient", 1.0);

        // Вычисляем, сколько раз игрок достиг кратного значения scoreStep
        int multiplierCount = (int) (playerScore / scoreStep);
        double coefficient = multiplierCount * coefficientStep;

        // Добавляем донатные бусты
        ConfigurationSection booster = CFG().getConfigurationSection("booster");
        if (booster != null) {
            for (String boosterKey : booster.getKeys(false)) {
                String permission = booster.getString(boosterKey + ".permission");
                if (permission != null && player.hasPermission(permission)) {
                    double boostValue = booster.getDouble(boosterKey + ".external-coefficient", 0.0);
                    coefficient += boostValue;
                }
            }
        }

        // Ограничиваем максимальным коэффициентом
        double maxLegalCoefficient = CFG().getDouble("max-legal-coefficient", 5.0);
        coefficient = Math.min(coefficient, maxLegalCoefficient);

        // Учитываем базовый коэффициент
        coefficient = Math.max(coefficient, defaultCoefficient);

        return Double.parseDouble(df.format(coefficient));
    }


    public static void addPlayerScores(Player player, double scores) {
        UUID playerId = player.getUniqueId();
        double currentScores = databaseManager.getPlayerScores(playerId.toString());
        databaseManager.addOrUpdatePlayerScores(playerId.toString(), currentScores + scores);
    }
}