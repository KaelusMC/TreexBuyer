package me.jetby.treexbuyer.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Menu implements InventoryHolder {
    String id;
    String titleMenu;
    Integer size;
    List<String> commandOpenMenu;
    String permissionOpenMenu;
    List<MenuButton> buttons;

    public Menu(String id, String titleMenu, Integer size, List<String> commandOpenMenu, String permissionOpenMenu, List<MenuButton> buttons) {
        this.id = id;
        this.titleMenu = titleMenu;
        this.size = size;
        this.commandOpenMenu = commandOpenMenu;
        this.permissionOpenMenu = permissionOpenMenu;
        this.buttons = buttons;
    }


    public String getId(){
        return id;
    }

    public String getTitleMenu() {
        return titleMenu;
    }

    public Integer getSize() {
        return size;
    }

    public List<String> getCommandOpenMenu() {
        return commandOpenMenu;
    }

    public String getPermissionOpenMenu() {
        return permissionOpenMenu;
    }

    public List<MenuButton> getButtons() {
        return buttons;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitleMenu(String titleMenu) {
        this.titleMenu = titleMenu;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setCommandOpenMenu(List<String> commandOpenMenu) {
        this.commandOpenMenu = commandOpenMenu;
    }

    public void setPermissionOpenMenu(String permissionOpenMenu) {
        this.permissionOpenMenu = permissionOpenMenu;
    }

    public void setButtons(List<MenuButton> buttons) {
        this.buttons = buttons;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
