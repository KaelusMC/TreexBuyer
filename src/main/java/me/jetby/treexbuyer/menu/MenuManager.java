package me.jetby.treexbuyer.menu;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.autoBuy.AutoBuy;
import me.jetby.treexbuyer.configurations.PriseItemLoader;
import me.jetby.treexbuyer.utils.Hex;
import me.jetby.treexbuyer.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.jetby.treexbuyer.autoBuy.AutoBuy.getAutoBuyStatus;
import static me.jetby.treexbuyer.boost.CoefficientManager.getPlayerCoefficient;
import static me.jetby.treexbuyer.boost.CoefficientManager.getPlayerScore;
import static me.jetby.treexbuyer.configurations.Config.CFG;
import static me.jetby.treexbuyer.menu.MenuListener.NAMESPACED_KEY;
import static me.jetby.treexbuyer.utils.Hex.hex;
import static me.jetby.treexbuyer.utils.Hex.setPlaceholders;

public class MenuManager {
    private final Map<String, Menu> listMenus;

    public MenuManager(Map<String, Menu> listMenus) {
        this.listMenus = listMenus;
    }

    private static final String TEST_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllY2NjNWMxYzc5YWE3ODI2YTE1YTdmNWYxMmZiNDAzMjgxNTdjNTI0MjE2NGJhMmFlZjQ3ZTVkZTlhNWNmYyJ9fX0=";

    public static void updateMenu(MenuButton button, Inventory topInventory, String count, Player player) {

        if (!button.getCommand().contains("[sell_zone]")) {



            ItemStack itemStack = button.getItemStackofMaterial();

            ItemMeta meta = itemStack.getItemMeta();

            PriseItemLoader.ItemData itemData = Main.getInstance().getPriseItem(button.getMaterialButton().name());
            double price = (itemData != null ? itemData.price() : 0);
            double price_with_coefficient = price * getPlayerCoefficient(player);
            double coefficient = getPlayerCoefficient(player);
            double score = getPlayerScore(player);

            String enabled = CFG().getString("autoBuy.enable", "&aВключён");
            String disabled = CFG().getString("autoBuy.disable", "&cВыключён");

            String global_auto_sell_toggle_string;

            if (meta != null) {
                if (button.getTitleButton() != null) {
                    meta.setDisplayName(hex(button.getTitleButton()));
                }
                boolean isSpecialButton = button.getCommand().contains("[AUTOBUY_ITEM_TOGGLE]") ||
                        button.getCommand().contains("[SELL_ALL]");
                boolean isStatusToggle = button.getCommand().contains("[AUTOBUY_STATUS_TOGGLE]");

                if (!isSpecialButton) {
                    meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_button");
                }

                UUID playerId = player.getUniqueId();
                List<String> autoBuyList = AutoBuy.getAutoBuyItems(playerId);
                if (autoBuyList != null && autoBuyList.contains(button.getMaterialButton().name())
                        && !meta.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING)) {

                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);


                } else if (isStatusToggle && getAutoBuyStatus(player.getUniqueId())) {
                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                }


                if (getAutoBuyStatus(player.getUniqueId())) {
                    global_auto_sell_toggle_string = enabled;

                } else {
                    global_auto_sell_toggle_string = disabled;

                }




                meta.setLore(button.getLoreButton().stream()
                        .map(s -> hex(setPlaceholders(player, s)))
                        .map(s -> s.replace("%price%", String.valueOf(price)))
                        .map(s -> s.replace("%coefficient%", String.valueOf(coefficient)))
                        .map(s -> s.replace("%score%", String.valueOf(score)))
                        .map(s -> s.replace("%sell_pay%", String.valueOf(count)))
                        .map(s -> s.replace("%price_with_coefficient%", String.valueOf(price_with_coefficient)))
                        .map(s -> s.replace("%auto_sell_toggle_state%", hex(autoBuyList != null && autoBuyList.contains(button.getMaterialButton().name())
                                ? CFG().getString("autoBuy.enable", "&aВключён")
                                : CFG().getString("autoBuy.disable", "&cВыключён"))))
                        .map(s -> s.replace("%global_auto_sell_toggle_state%", hex(global_auto_sell_toggle_string)))
                        .toList());

                itemStack.setItemMeta(meta);
            }
            topInventory.setItem(button.getSlotButton(), itemStack);
        }
    }


    public void openMenu(Player player, String menuName) {
        Menu menu = listMenus.get(menuName);
        if (menu == null) {
            player.sendMessage("Меню не найдено!");
            return;
        }

        Inventory inventory = Bukkit.createInventory(menu, menu.getSize(), hex(menu.getTitleMenu()));


        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.openInventory(inventory), 1L);
    }

    public Map<String, Menu> getListMenu(){
        return listMenus;
    }


}
