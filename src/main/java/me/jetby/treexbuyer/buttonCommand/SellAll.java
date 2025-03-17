package me.jetby.treexbuyer.buttonCommand;

import me.jetby.treexbuyer.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellAll {
    public SellAll() {
    }

    public static Inventory sellAll(Double count, Player player, List<ItemStack> itemStacks, Map<String, Double> prise, Inventory topInventory) {
        if (count > (double)0.0F) {
            Main.getInstance().economy.depositPlayer(player, count);
            Map<Material, Double> materialPrise = new HashMap();
            prise.forEach((key, vault) -> {
                Material material = Material.valueOf(key.toUpperCase());
                materialPrise.put(material, vault);
            });

            for(int i = 0; i < topInventory.getSize(); ++i) {
                ItemStack currentItem = topInventory.getItem(i);
                if (currentItem != null && itemStacks.contains(currentItem)) {
                    topInventory.setItem(i, (ItemStack)null);
                    Main.getInstance().getLogger().info("Удалён предмет из слота: " + i);
                    itemStacks.remove(currentItem);
                }
            }

            return topInventory;
        } else {
            return topInventory;
        }
    }
}
