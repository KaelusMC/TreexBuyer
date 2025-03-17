package me.jetby.treexbuyer.Menu;

import org.bukkit.Material;

import java.util.List;

public class MenuButton {
    Integer slotButton;
    String TitleButton;
    boolean hide_enchantments;
    boolean hide_attributes;
    boolean enchanted;
    List<String> loreButton;
    Material materialButton;
    List<String> command;

    public MenuButton(Integer slotButton,
                      String titleButton,
                      List<String> loreButton,
                      Material materialButton,
                      List<String> command,
                      boolean hide_enchantments,
                      boolean hide_attributes,
                      boolean enchanted) {
        this.slotButton = slotButton;
        TitleButton = titleButton;
        this.loreButton = loreButton;
        this.materialButton = materialButton;
        this.command = command;
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
        return materialButton;
    }

    public List<String> getCommand() {
        return command;
    }

    public void setSlotButton(Integer slotButton) {
        this.slotButton = slotButton;
    }

    public void setTitleButton(String titleButton) {
        TitleButton = titleButton;
    }

    public void setLoreButton(List<String> loreButton) {
        this.loreButton = loreButton;
    }

    public void setMaterialButton(Material materialButton) {
        this.materialButton = materialButton;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }
}
