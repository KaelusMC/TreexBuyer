package me.jetby.treexbuyer.menu;

import me.jetby.treexbuyer.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuButton {
    Integer slotButton;
    String TitleButton;
    boolean hide_enchantments;
    boolean hide_attributes;
    boolean enchanted;
    List<String> loreButton;
    String materialButton;
    List<String> command;
    Map<ClickType, List<String>> commands; // Изменяем на Map для хранения команд по типам кликов

    public MenuButton(Integer slotButton,
                      String titleButton,
                      List<String> loreButton,
                      String materialButton,
                      Map<ClickType, List<String>> commands,
                      List<String> command,
                      boolean hide_enchantments,
                      boolean hide_attributes,
                      boolean enchanted) {
        this.slotButton = slotButton;
        TitleButton = titleButton;
        this.loreButton = loreButton;
        this.command = command;
        this.materialButton = materialButton;
        this.commands = commands;
        this.hide_enchantments = hide_enchantments;
        this.hide_attributes = hide_attributes;
        this.enchanted = enchanted;
    }


    public boolean isEnchanted() {
        return enchanted;
    }

    public boolean isHide_attributes() {
        return hide_attributes;
    }

    public boolean isHide_enchantments() {
        return hide_enchantments;
    }

    public Integer getSlotButton() {
        return slotButton;
    }

    public String getTitleButton() {
        return TitleButton;
    }

    public List<String> getLoreButton() {
        return loreButton;
    }

    public Material getMaterialButton() {

        if (materialButton.startsWith("basehead-")) {
            return Material.valueOf(SkullCreator.createSkull().getType().name());

        }


        return Material.valueOf(materialButton);
    }

    public ItemStack getItemStackofMaterial() {

        ItemStack itemStack;
        if (materialButton.startsWith("basehead-")) {
            try {
                itemStack = SkullCreator.itemFromBase64(materialButton.replace("basehead-", ""));
            } catch (Exception e) {
                Bukkit.getLogger().warning("Ошибка при создании кастомного черепа: " + e.getMessage());
                itemStack = new ItemStack(Material.PLAYER_HEAD);
            }
        } else {
            itemStack = new ItemStack(Material.valueOf(materialButton));
        }

        return itemStack;
    }

    public Map<ClickType, List<String>> getCommands() {
        return commands;
    }

    public List<String> getAllCommands() {
        List<String> allCommands = new ArrayList<>(command);
        for (List<String> commands : this.commands.values()) {
            allCommands.addAll(commands);
        }
        return allCommands;
    }

    public boolean isSellZone() {
        return command.contains("[sell_zone]");
    }

    public List<String> getCommand() {
        return command;
    }

}
