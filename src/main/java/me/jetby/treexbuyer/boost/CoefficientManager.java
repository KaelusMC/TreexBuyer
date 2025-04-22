package me.jetby.treexbuyer.boost;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.configurations.Config;
import me.jetby.treexbuyer.dataBase.DatabaseUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.jetby.treexbuyer.configurations.Config.CFG;

public class CoefficientManager {
    private static final Map<UUID, Double> cachedScores = new HashMap<>();

    public static Map<UUID, Double> getCachedScores() {
        return cachedScores;
    }

    public static final DecimalFormat df = new DecimalFormat("#.##");


    public static void loadPlayerScoreAsync(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Double result = DatabaseUtils.get("players", uuid, "scores", rs -> {
                try {
                    return rs.getDouble("scores");
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0.0;
                }
            });

            double score = result == null ? 0.0 : result;
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                cachedScores.put(uuid, score);
            });
        });
    }




    public static double getPlayerCoefficient(Player player) {


        double defaultCoefficient = CFG().getDouble("default-coefficient", 1.0);
        double playerScore = getCachedScore(player);


        int scoreStep = CFG().getInt("score-to-multiplier-ratio.scores", 100);
        double coefficientStep = CFG().getDouble("score-to-multiplier-ratio.coefficient", 1.0);

        int multiplierCount = (int) (playerScore / scoreStep);
        double coefficient = multiplierCount * coefficientStep;

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

        double maxLegalCoefficient = CFG().getDouble("max-legal-coefficient", 5.0);
        coefficient = Math.min(coefficient, maxLegalCoefficient);
        coefficient = Math.max(coefficient, defaultCoefficient);

        return Double.parseDouble(df.format(coefficient));
    }
    public static double getCachedScore(Player player) {
        return cachedScores.getOrDefault(player.getUniqueId(), 0.0);
    }
    public static void addPlayerScores(Player player, double scores) {
        UUID uuid = player.getUniqueId();
        double current = cachedScores.getOrDefault(uuid, 0.0);
        cachedScores.put(uuid, current + scores);
    }
}
