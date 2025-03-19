package me.jetby.treexbuyer.menuListener;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.menu.Actions;
import me.jetby.treexbuyer.menu.Menu;
import me.jetby.treexbuyer.menu.MenuButton;
import me.jetby.treexbuyer.autoBuy.AutoBuy;
import me.jetby.treexbuyer.buttonCommand.SellZone;
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
import java.util.Map;
import java.util.UUID;

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

        if (!clickedInventory.equals(topInventory)) {
            return;
        }

        for (MenuButton button : menu.getButtons()) {
            if (event.getSlot() == button.getSlotButton()) {
                for (String command : button.getCommand()) {
                    switch (command.toLowerCase()) {

                        case "[sell_zone]":
                            return;

                        case "[sell_all]":
                            event.setCancelled(true);

                            List<ItemStack> itemStacks = new ArrayList<>();
                            for (MenuButton btn : menu.getButtons()) {
                                if (btn.getCommand().contains("[sell_zone]")) {
                                    ItemStack item = topInventory.getItem(btn.getSlotButton());
                                    if (item != null) {
                                        itemStacks.add(item);
                                    }
                                }
                            }

                            double count = SellZone.getCountPlayer(player.getUniqueId());
                            if (count > 0) {
                                Main.getInstance().economy.depositPlayer(player, count);
                                for (int i = 0; i < topInventory.getSize(); i++) {
                                    ItemStack itemStack = topInventory.getItem(i);
                                    if (itemStack != null) {
                                        Map<Material, Double> materialPriceMap = Main.getInstance().getMaterialPrise(Main.getInstance().getItemPrise());
                                        int finalI = i;

                                        menu.getButtons().forEach(btn -> {
                                            if (btn.getSlotButton() == finalI && btn.getCommand().contains("[sell_zone]")) {
                                                if (materialPriceMap.containsKey(itemStack.getType())) {
                                                    topInventory.setItem(finalI, null);
                                                }
                                            }
                                        });
                                    }
                                }
                                player.sendMessage(hex(CFG().getString("completeSaleMessage").replace("%sum%", String.valueOf(count))));
                            } else {
                                player.sendMessage(hex(CFG().getString("noItemsToSellMessage", "У вас нет предметов для продажи")));
                                break;
                            }
                            break;

                        case "[sell_item]":
                            Material materialSell = button.getMaterialButton();
                            Double itemPrice = Main.getInstance().getPriseItem(materialSell.name().toLowerCase());

                            if (itemPrice != null) {
                                PlayerInventory inventory = player.getInventory();
                                double totalSum = 0d;

                                for (int slot = 0; slot < 36; slot++) {
                                    ItemStack item = inventory.getItem(slot);
                                    if (item != null && item.getType() == materialSell) {
                                        double itemTotalPrice = itemPrice * item.getAmount();
                                        Main.getInstance().economy.depositPlayer(player, itemTotalPrice);
                                        totalSum += itemTotalPrice;
                                        player.getInventory().setItem(slot, null);
                                    }
                                }
                                player.sendMessage(hex("&eВы продали предметы на сумму: &a" + totalSum));
                            }
                            break;

                        case "[auto_buy]":
                            event.setCancelled(true);
                            UUID playerId = player.getUniqueId();
                            String materialName = button.getMaterialButton().name();

                            AutoBuy.toggleAutoBuyItem(playerId, materialName);
                            updateLoreButton(button, topInventory, SellZone.getCountPlayerString(playerId, 0), player);
                            break;

                        default:
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

            Double price = Main.getInstance().getPriseItem(button.getMaterialButton().name());

            if (meta != null) {
                if (button.getTitleButton()!=null) {
                    meta.setDisplayName(hex(button.getTitleButton()));
                }

                UUID playerId = player.getUniqueId();
                List<String> autoBuyList = AutoBuy.getAutoBuyPlayers(playerId);
                if (autoBuyList != null && autoBuyList.contains(button.getMaterialButton().name())) {
                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.setLore(button.getLoreButton().stream()
                            .map(s -> hex(setPlaceholders(player, s)))
                            .map(s -> s.replace("%price%", String.valueOf(price)))
                            .map(s -> s.replace("%auto_sell_toggle_state%", hex(CFG().getString("autoBuy.enable", "&aВключён"))))
                            .map(s -> s.replace("%seller_pay%", count)).toList());

                } else {
                    meta.removeEnchant(Enchantment.LUCK);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.setLore(button.getLoreButton().stream()
                            .map(s -> hex(setPlaceholders(player, s)))
                            .map(s -> s.replace("%price%", String.valueOf(price)))
                            .map(s -> s.replace("%auto_sell_toggle_state%", hex(CFG().getString("autoBuy.disable", "&cВыключён"))))
                            .map(s -> s.replace("%seller_pay%", count)).toList());

                }

                itemStack.setItemMeta(meta);
            }
            topInventory.setItem(button.getSlotButton(), itemStack);
        }
    }

}

