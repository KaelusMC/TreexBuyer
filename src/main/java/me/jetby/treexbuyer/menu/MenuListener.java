package me.jetby.treexbuyer.menu;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.autoBuy.AutoBuy;
import me.jetby.treexbuyer.configurations.PriseItemLoader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.jetby.treexbuyer.autoBuy.AutoBuy.getAutoBuyStatus;
import static me.jetby.treexbuyer.autoBuy.AutoBuy.setAutoBuyStatus;
import static me.jetby.treexbuyer.boost.CoefficientManager.*;
import static me.jetby.treexbuyer.configurations.Config.CFG;
import static me.jetby.treexbuyer.menu.MenuManager.updateMenu;
import static me.jetby.treexbuyer.utils.Hex.hex;

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


        if (event.isShiftClick() && clickedInventory.equals(player.getInventory())) {
            boolean foundSellZone = false;

            for (MenuButton button : menu.getButtons()) {
                if (button.isSellZone()) {
                    ItemStack itemInSlot = topInventory.getItem(button.getSlotButton());
                    if (itemInSlot == null) {
                        foundSellZone = true;
                        break;
                    }
                }
            }

            if (!foundSellZone) {
                event.setCancelled(true);
                return;
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
                        if (btn.getSlotButton() == i && btn.isSellZone()) {
                            itemStacks.add(itemStack);
                            break;
                        }
                    }
                }
            }

            SellZone.checkItem(itemStacks, Main.getInstance().getItemPrice(), player);
            for (MenuButton btn : menu.getButtons()) {
                updateMenu(btn,
                        topInventory,
                        SellZone.getCountPlayerString(player.getUniqueId(), Integer.valueOf(String.valueOf(df.format(finalTotalScores)))),
                        player);
            }
        }, 1L);



        boolean isSellZoneSlot = false;
        for (MenuButton button : menu.getButtons()) {
            if (button.isSellZone() && event.getSlot() == button.getSlotButton()) {
                isSellZoneSlot = true;
                break;
            }
        }

        if (!isSellZoneSlot) {
            event.setCancelled(true);
        }

        if (clickedInventory.equals(topInventory)) {
            // Проверяем клик по кнопке
            for (MenuButton button : menu.getButtons()) {
                if (event.getSlot() == button.getSlotButton()) {


                    if (button.isSellZone()) {
                        return;
                    }

                    List<String> allCommands = button.getAllCommands();

                    for (String command : allCommands) {
                        if (command.equalsIgnoreCase("[sell_all]")) {
                            event.setCancelled(true);
                            for (MenuButton btn : menu.getButtons()) {
                                if (btn.isSellZone()) {
                                    ItemStack item = topInventory.getItem(btn.getSlotButton());

                                    if (item != null) {
                                        PriseItemLoader.ItemData itemData = Main.getInstance().getPriseItem(item.getType().name());
                                        if (itemData != null) {
                                            double price = itemData.price() * getPlayerCoefficient(player);
                                            int addScores = itemData.addScores();
                                            totalMoney += price * item.getAmount();
                                            totalScores += addScores * item.getAmount();
                                        }
                                    } break;
                                }
                            }

                            if (totalMoney > 0 || totalScores > 0) {
                                Main.getInstance().economy.depositPlayer(player, totalMoney);

                                for (MenuButton btn : menu.getButtons()) {
                                    if (btn.isSellZone()) {
                                        ItemStack item = topInventory.getItem(btn.getSlotButton());
                                        if (item != null) {
                                            PriseItemLoader.ItemData itemData = Main.getInstance().getPriseItem(item.getType().name());
                                            if (itemData != null) {
                                                topInventory.setItem(btn.getSlotButton(), null);
                                            }

                                        }
                                    }
                                }

                                addPlayerScores(player, totalScores);
                                player.sendMessage(hex(CFG().getString("completeSaleMessage", "&eВы продали предметы на сумму: &a%sell_pay% &eи получили &b%sell_score% очков")
                                        .replace("%sell_pay%", String.valueOf(totalMoney))
                                        .replace("%sell_score%", String.valueOf(totalScores))));

                            } else {
                                player.sendMessage(hex(CFG().getString("noItemsToSellMessage", "У вас нет предметов для продажи")));
                            }
                        } else {
                            event.setCancelled(true);
                            Map<ClickType, List<String>> commandsMap = button.getCommands();
                            ClickType clickType = event.getClick();

                            // Получаем команды ТОЛЬКО для конкретного типа клика
                            List<String> commands = commandsMap.get(clickType);

                            // Если команды найдены, выполняем их
                            if (commands != null && !commands.isEmpty()) {
                                for (String actions : commands) {
                                    executeCommand(player, actions, button);
                                }
                            }
                        }

                        break;
                    }
                    return;
                }
            }

        }
    }
    private void executeCommand(Player player, String command, MenuButton button) {

        switch (command.toLowerCase()) {

            case "[autobuy_item_toggle]": {
                UUID playerId = player.getUniqueId();
                String materialName = button.getMaterialButton().name();
                AutoBuy.toggleAutoBuyItem(playerId, materialName);
                updateMenu(button, player.getOpenInventory().getTopInventory(),
                        SellZone.getCountPlayerString(playerId, 0), player);

                break;
            }


            case "[autobuy_status_toggle]": {
                setAutoBuyStatus(player.getUniqueId(), !getAutoBuyStatus(player.getUniqueId()));
                updateMenu(button, player.getOpenInventory().getTopInventory(),
                        SellZone.getCountPlayerString(player.getUniqueId(), 0), player);
                break;
            }


            case "[enable_all]": {
                UUID playerIdEnable = player.getUniqueId();
                for (ItemStack content : player.getOpenInventory().getTopInventory().getContents()) {
                    if (content == null) continue;
                    ItemMeta contentMeta = content.getItemMeta();
                    if (contentMeta == null) continue;
                    if (contentMeta.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.INTEGER)) continue;
                    AutoBuy.enableAutBuyItem(playerIdEnable, content.getType().name());
                }
                updateMenu(button, player.getOpenInventory().getTopInventory(),
                        SellZone.getCountPlayerString(playerIdEnable, 0), player);
                break;
            }


            case "[disable_all]": {
                UUID playerIdDisable = player.getUniqueId();
                for (ItemStack content : player.getOpenInventory().getTopInventory().getContents()) {
                    if (content == null) continue;
                    ItemMeta contentMeta = content.getItemMeta();
                    if (contentMeta == null) continue;
                    if (contentMeta.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.INTEGER)) continue;
                    AutoBuy.disableAutBuyItem(playerIdDisable, content.getType().name());
                }
                break;
            }

            default: {
                Actions.execute(player, command);
                break;
            }

        }
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
                if (slot.equals(button.getSlotButton()) && button.isSellZone()) {
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
                        if (btn.getSlotButton() == i && btn.isSellZone()) {
                            itemStacks.add(itemStack);
                            break;
                        }
                    }
                }
            }
            SellZone.checkItem(itemStacks, Main.getInstance().getItemPrice(), player);
            for (MenuButton btn : menu.getButtons()) {
                updateMenu(btn, topInventory, SellZone.getCountPlayerString(player.getUniqueId(), 0), player);
            }
        }, 1L);
    }
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }


        for (MenuButton btn : menu.getButtons()) {
            List<ItemStack> itemStacks = new ArrayList<>();
            SellZone.checkItem(itemStacks, Main.getInstance().getItemPrice(), player);


            updateMenu(btn, topInventory, SellZone.getCountPlayerString(player.getUniqueId(), 0), player);

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
                .filter(MenuButton::isSellZone)
                .forEach(button -> {
                    ItemStack item = topInventory.getItem(button.getSlotButton());
                    if (item != null && !item.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING)) {
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

    public static NamespacedKey NAMESPACED_KEY = new NamespacedKey("treexbuyer", "key");






}

