package me.jetby.treexbuyer.menu;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.autoBuy.AutoBuy;
import me.jetby.treexbuyer.configurations.PriseItemLoader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.jetby.treexbuyer.boost.CoefficientManager.*;
import static me.jetby.treexbuyer.configurations.Config.CFG;
import static me.jetby.treexbuyer.utils.Hex.hex;
import static me.jetby.treexbuyer.utils.Hex.setPlaceholders;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }

        if (event.isShiftClick() && (!clickedInventory.equals(topInventory))) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                return;
            }

            boolean hasFreeSlot = false;
            for (MenuButton button : menu.getButtons()) {
                if (button.getCommand().contains("[sell_zone]")) {
                    int slot = button.getSlotButton();
                    ItemStack itemInSlot = topInventory.getItem(slot);
                    if (itemInSlot == null) {
                        hasFreeSlot = true;
                        break;
                    }
                }
            }

            if (!hasFreeSlot) {
                event.setCancelled(true);
            } else {
                for (MenuButton button : menu.getButtons()) {
                    if (button.getCommand().contains("[sell_zone]")) {
                        int slot = button.getSlotButton();
                        ItemStack itemInSlot = topInventory.getItem(slot);
                        if (itemInSlot == null) {
                            topInventory.setItem(slot, clickedItem);
                            event.setCurrentItem(null);
                            break;
                        }
                    }
                }
            }
        }
        double totalMoney = 0;
        double totalScores = 0;
        double finalTotalScores = totalScores;
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < topInventory.getSize(); i++) {
                ItemStack itemStack = topInventory.getItem(i);
                if (itemStack != null) {
                    for (MenuButton btn : menu.getButtons()) {
                        if (btn.getSlotButton() == i && btn.getCommand().contains("[sell_zone]")) {
                            itemStacks.add(itemStack);
                            break;
                        }
                    }
                }
            }

            SellZone.checkItem(itemStacks, Main.getInstance().getItemPrise(), player);
            for (MenuButton btn : menu.getButtons()) {
                updateLoreButton(btn, topInventory, SellZone.getCountPlayerString(player.getUniqueId(), Integer.valueOf(String.valueOf(df.format(finalTotalScores)))), player);
            }
        }, 1L);

        if (!clickedInventory.equals(topInventory)) {
            return;
        }

        for (MenuButton button : menu.getButtons()) {
            if (event.getSlot() == button.getSlotButton()) {
                for (String command : button.getCommand()) {
                    switch (command.toLowerCase()) {

                        case "[sell_zone]":
                            return;

                        case "[sell_all]": {
                            event.setCancelled(true);

                            // Собираем предметы из sell_zone слотов
                            for (MenuButton btn : menu.getButtons()) {
                                if (btn.getCommand().contains("[sell_zone]")) {
                                    ItemStack item = topInventory.getItem(btn.getSlotButton());

                                    if (item!=null) {
                                        PriseItemLoader.ItemData itemData = Main.getInstance().getPriseItem(item.getType().name());
                                        if (itemData != null) {
                                            double price = itemData.price() * getPlayerCoefficient(player);
                                            int addScores = itemData.addScores();
                                            totalMoney += price * item.getAmount();
                                            totalScores += addScores * item.getAmount();
                                        }
                                    }
                                }
                            }



                            if (totalMoney > 0 || totalScores > 0) {
                                Main.getInstance().economy.depositPlayer(player, totalMoney);

                                // Очищаем sell_zone слоты
                                for (MenuButton btn : menu.getButtons()) {
                                    if (btn.getCommand().contains("[sell_zone]")) {
                                        ItemStack item = topInventory.getItem(btn.getSlotButton());
                                        if (item != null) {
                                            topInventory.setItem(btn.getSlotButton(), null);
                                        }
                                    }
                                }
                                addPlayerScores(player, totalScores);

                                player.sendMessage(hex(CFG().getString(
                                                "completeSaleMessage", "&eВы продали предметы на сумму: &a%sum% &eи получили &b%score% очков")
                                        .replace("%sum%", String.valueOf(totalMoney))
                                        .replace("%score%", String.valueOf(totalScores))
                                ));
                            } else {
                                player.sendMessage(hex(CFG().getString("noItemsToSellMessage", "У вас нет предметов для продажи")));
                            }
                            break;
                        }



                        case "[sell_item]": {
                            Material materialSell = button.getMaterialButton();
                            PriseItemLoader.ItemData itemData = Main.getInstance().getPriseItem(materialSell.name().toLowerCase());

                            if (itemData != null) {
                                double itemPrice = itemData.price();
                                int addScores = itemData.addScores();

                                PlayerInventory inventory = player.getInventory();
                                double totalSum = 0d;
                                totalScores = 0;

                                for (int slot = 0; slot < 36; slot++) {
                                    ItemStack item = inventory.getItem(slot);
                                    if (item != null && item.getType() == materialSell) {
                                        double itemTotalPrice = itemPrice * item.getAmount();
                                        int itemTotalScores = addScores * item.getAmount();
                                        Main.getInstance().economy.depositPlayer(player, itemTotalPrice);
                                        totalSum += itemTotalPrice;
                                        totalScores += itemTotalScores;
                                        player.getInventory().setItem(slot, null);
                                    }
                                }

                                player.sendMessage(hex(CFG().getString(
                                        "completeSaleMessage", "&eВы продали предметы на сумму: &a%sum% &eи получили &b%score% очков")
                                        .replace("%sum%", String.valueOf(totalSum))
                                        .replace("%score%", String.valueOf(totalScores))
                                ));
                            }
                            break;
                        }


                        case "[auto_buy]": {
                            event.setCancelled(true);
                            UUID playerId = player.getUniqueId();
                            String materialName = button.getMaterialButton().name();

                            AutoBuy.toggleAutoBuyItem(playerId, materialName);
                            updateLoreButton(button, topInventory, SellZone.getCountPlayerString(playerId, 0), player);
                            break;
                        }


                        default: {
                            if (command.startsWith("[open_menu]")) {
                                event.setCancelled(true);
                                String key = command.substring("[open_menu]".length()).trim();
                                Main.getInstance().getMenuManager().openMenu(player, key);
                            } else {
                                event.setCancelled(true);
                                Actions.execute(player, command);
                            }
                            break;
                        }

                    }
                }
                break;
            }
        }


        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getInventory();

        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        for (Integer slot : event.getRawSlots()) {
            if (slot >= topInventory.getSize()) {
                continue;
            }

            boolean isAllowedSlot = false;

            for (MenuButton button : menu.getButtons()) {
                if (slot == button.getSlotButton() && button.getCommand().contains("[sell_zone]")) {
                    isAllowedSlot = true;
                    break;
                }
            }

            if (!isAllowedSlot) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < topInventory.getSize(); i++) {
                ItemStack itemStack = topInventory.getItem(i);
                if (itemStack != null) {

                    for (MenuButton btn : menu.getButtons()) {
                        if (btn.getSlotButton() == i && btn.getCommand().contains("[sell_zone]")) {
                            itemStacks.add(itemStack);
                            break;
                        }
                    }
                }
            }
            SellZone.checkItem(itemStacks, Main.getInstance().getItemPrise(), player);
            for (MenuButton btn : menu.getButtons()) {
                updateLoreButton(btn, topInventory, SellZone.getCountPlayerString(player.getUniqueId(), 0), player);
            }
        }, 1L);
    }
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }


        for (MenuButton btn : menu.getButtons()) {
            List<ItemStack> itemStacks = new ArrayList<>();
            SellZone.checkItem(itemStacks, Main.getInstance().getItemPrise(), player);


            updateLoreButton(btn, topInventory, SellZone.getCountPlayerString(player.getUniqueId(), 0), player);

        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }

        menu.getButtons().stream()
                .filter(button -> button.getCommand().contains("[sell_zone]"))
                .forEach(button -> {
                    ItemStack item = topInventory.getItem(button.getSlotButton());
                    if (item != null) {
                        if (player.getInventory().firstEmpty() == -1) {
                            player.getWorld().dropItem(player.getLocation(), item);
                        } else {
                            player.getInventory().addItem(item);
                        }
                        topInventory.setItem(button.getSlotButton(), null);
                    }
                });

        SellZone.clearPlayer(playerUUID);
    }

    public static void updateLoreButton(MenuButton button, Inventory topInventory, String count, Player player) {
        if (!button.getCommand().contains("[sell_zone]")) {
            ItemStack itemStack = new ItemStack(button.getMaterialButton());
            ItemMeta meta = itemStack.getItemMeta();

            PriseItemLoader.ItemData itemData = Main.getInstance().getPriseItem(button.getMaterialButton().name());
            double price = (itemData != null ? itemData.price() : 0);
            double price_with_coefficient = price * getPlayerCoefficient(player);
            double coefficient = getPlayerCoefficient(player);
            double score = getPlayerScore(player);

            if (meta != null) {
                if (button.getTitleButton() != null) {
                    meta.setDisplayName(hex(button.getTitleButton()));
                }

                UUID playerId = player.getUniqueId();
                List<String> autoBuyList = AutoBuy.getAutoBuyPlayers(playerId);
                if (autoBuyList != null && autoBuyList.contains(button.getMaterialButton().name())) {
                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else {
                    meta.removeEnchant(Enchantment.LUCK);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                meta.setLore(button.getLoreButton().stream()
                        .map(s -> hex(setPlaceholders(player, s)))
                        .map(s -> s.replace("%price%", String.valueOf(price)))
                        .map(s -> s.replace("%coefficient%", String.valueOf(coefficient)))
                        .map(s -> s.replace("%score%", String.valueOf(score)))
                        .map(s -> s.replace("%seller_pay%", String.valueOf(count)))
                        .map(s -> s.replace("%price_with_coefficient%", String.valueOf(price_with_coefficient)))
                        .map(s -> s.replace("%auto_sell_toggle_state%", hex(autoBuyList != null && autoBuyList.contains(button.getMaterialButton().name())
                                ? CFG().getString("autoBuy.enable", "&aВключён")
                                : CFG().getString("autoBuy.disable", "&cВыключён"))))
                        .toList());

                itemStack.setItemMeta(meta);
            }
            topInventory.setItem(button.getSlotButton(), itemStack);
        }
    }


}

