package me.jetby.treexbuyer.menu;

import me.jetby.treexbuyer.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getLogger;

public class SellAll {
    public SellAll() {
    }

    public static Inventory sellAll(Double count, Player player, List<ItemStack> itemStacks, Map<String, Double> prise, Inventory topInventory) {
        if (count > 0.0F) {
            Main.getInstance().economy.depositPlayer(player, count);
            Map<Material, Double> materialPrise = new HashMap();
            prise.forEach((key, vault) -> {
                Material material = Material.valueOf(key.toUpperCase());
                materialPrise.put(material, vault);
            });

            for(int i = 0; i < topInventory.getSize(); ++i) {
                ItemStack currentItem = topInventory.getItem(i);
                if (currentItem != null && itemStacks.contains(currentItem)) {
                    topInventory.setItem(i, null);
                    getLogger().info("Удалён предмет из слота: " + i);
                    itemStacks.remove(currentItem);
                }
            }

        }
        return topInventory;
    }
}
