package me.jetby.treexbuyer.menuManager;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.menu.Menu;
import me.jetby.treexbuyer.autoBuy.AutoBuy;
import me.jetby.treexbuyer.utils.Hex;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.jetby.treexbuyer.Main.getCfg;
import static me.jetby.treexbuyer.utils.Hex.hex;
import static me.jetby.treexbuyer.utils.Hex.setPlaceholders;

public class MenuManager {
    private final Map<String, Menu> listMenus;

    public MenuManager(Map<String, Menu> listMenus) {
        this.listMenus = listMenus;
    }
    public void openMenu(Player player, String menuName) {
        Menu menu = listMenus.get(menuName);
        if (menu == null) {
            player.sendMessage("Меню не найдено!");
            return;
        }

        Inventory inventory = Bukkit.createInventory(menu, menu.getSize(), menu.getTitleMenu());

        menu.getButtons().forEach(entry -> {
            ItemStack itemStack = new ItemStack((entry.getMaterialButton()));
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                if (entry.getTitleButton()!=null) {
                    meta.setDisplayName(hex(entry.getTitleButton()));
                }
                UUID playerId = player.getUniqueId();
                List<String> autoBuyList = AutoBuy.getAutoBuyPlayers(playerId);
                if (autoBuyList != null && autoBuyList.contains(entry.getMaterialButton().name())) {

                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.setLore(entry.getLoreButton().stream()
                            .map(s -> Hex.hex(setPlaceholders(player, s)))
                            .map(s -> s.replace("%auto_sell_toggle_state%", hex(getCfg().getString("autoBuy.enable", "&aВключён"))))
                            .map(s -> s.replace("%seller_pay%", "0")).toList());

                } else {
                    meta.setLore(entry.getLoreButton().stream()
                            .map(s -> Hex.hex(setPlaceholders(player, s)))
                            .map(s -> s.replace("%auto_sell_toggle_state%", hex(getCfg().getString("autoBuy.disable", "&cВыключен"))))
                            .map(s -> s.replace("%seller_pay%", "0")).toList());


                    meta.removeEnchant(Enchantment.LUCK);
                    meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                }




                itemStack.setItemMeta(meta);
            }
            int slot = entry.getSlotButton();
            if (slot >= 0 && slot < menu.getSize()) {
                inventory.setItem(slot, itemStack);
            }
        });

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.openInventory(inventory), 1L);
    }

    public Map<String, Menu> getListMenu(){
        return listMenus;
    }


}
