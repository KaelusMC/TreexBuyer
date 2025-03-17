package me.jetby.treexbuyer.buttonCommand;

import me.jetby.treexbuyer.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class SellZone {

    private static Map<UUID, Double> countPlayer = new HashMap<>();
    public static void checkItem(List<ItemStack> itemStacks, Map<String, Double> prise, Player player) {
        countPlayer.put(player.getUniqueId(), 0d); // Вместо удаления сразу ставим 0

        Map<Material, Double> materialPrise = new HashMap<>();
        prise.forEach((key, vault) -> {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                materialPrise.put(material, vault);
            } catch (IllegalArgumentException e) {
                Main.getInstance().getLogger().warning("[SellZone] Неизвестный материал: " + key);
            }
        });

//        itemStacks.forEach(item -> Main.getInstance().getLogger().info(item.getType() + " : " + item.getAmount()));

        double sum = 0d;
        for (ItemStack itemStack : itemStacks) {
            Double price = materialPrise.get(itemStack.getType());
            if (price != null) {
                sum += price * itemStack.getAmount();
            }
        }

        countPlayer.put(player.getUniqueId(), sum);
    }


    public static Double getCountPlayer(UUID uuid){
        return countPlayer.get(uuid);
    }
    public static void clearPlayer(UUID uuid){
        countPlayer.remove(uuid);

    }
    public static String getCountPlayerString(UUID uuid, Integer residue) {
        Double value = countPlayer.get(uuid);
        if (value == null) {
            return "0"; // Или другое значение по умолчанию
        }

        // Создаем BigDecimal из значения, чтобы можно было легко округлять
        BigDecimal bd = new BigDecimal(value);

        // Округляем до необходимого количества знаков после запятой
        bd = bd.setScale(residue, RoundingMode.DOWN); // RoundingMode.DOWN убирает остаток

        // Возвращаем строковое представление числа
        return bd.toPlainString(); // Используем toPlainString, чтобы избежать научной нотации
    }
}