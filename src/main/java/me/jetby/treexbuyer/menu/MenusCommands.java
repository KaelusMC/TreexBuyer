package me.jetby.treexbuyer.menu;

import me.jetby.treexbuyer.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenusCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только игрокам.");
            return true;
        }
        Main.getInstance().getMenuManager().getListMenu().forEach((key, vault) ->{
            vault.getCommandOpenMenu().forEach(item -> {
                if (item.equals(label)){
                    Main.getInstance().getMenuManager().openMenu(player, key);
                }
            });
        });

        return false;
    }
}
