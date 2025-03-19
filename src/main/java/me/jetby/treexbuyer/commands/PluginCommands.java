package me.jetby.treexbuyer.commands;

import me.jetby.treexbuyer.Main;
import me.jetby.treexbuyer.configurations.Config;
import me.jetby.treexbuyer.configurations.PriceItemCfg;
import me.jetby.treexbuyer.loader.MenuLoader;
import me.jetby.treexbuyer.menuManager.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexbuyer.configurations.Config.CFG;
import static me.jetby.treexbuyer.utils.Hex.hex;

public class PluginCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        menuManager = new MenuManager(MenuLoader.getListMenu());



        if (args.length==0) {
            if (!sender.hasPermission("treexbuyer.admin")) {
                return true;
            }

            if (sender instanceof Player player) {
                player.sendMessage(hex("&aНе пон."));
            } else {
                sender.sendMessage("хз.");
            }

            return true;
        }


        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("treexbuyer.admin")) {
                return true;
            }

            Config cfg = new Config();
            PriceItemCfg price = new PriceItemCfg();

            Main plugin = Main.getInstance();

            cfg.reloadCfg(plugin);
            price.reloadCfg(plugin);
            MenuLoader.loadMenus(CFG(), plugin.getDataFolder());

            if (sender instanceof Player player) {
                player.sendMessage(hex("&aУспешная перезагрузка."));
            } else {
                sender.sendMessage("Успешная перезагрузка.");
            }


            return true;
        }

        if (args[0].equalsIgnoreCase("open")) {
            if (!sender.hasPermission("treexbuyer.admin")) {
                return true;
            }




            if (args.length==2) {
                if (sender instanceof Player player) {
                    menuManager.openMenu(player, args[1]);
                } else {
                    sender.sendMessage("Данная команда доступа только для игроков.");
                }


            } else if (args.length>2) {
                Player player = Bukkit.getPlayer(args[2]);
                if (player!=null) {
                    menuManager.openMenu(player, args[1]);
                } else {
                    if (sender instanceof Player p) p.sendMessage(hex("&b&lTreexBuyer &7▶ &cИгрок не найден."));
                    else {
                        sender.sendMessage("[TreexBuyer] ▶ Игрок не найден.");
                    }
                }

            } else {
                if (sender instanceof Player player) {
                    player.sendMessage(hex("&b&lTreexBuyer &7▶ &fИспользование &b/" + cmd.getName() + " open <id> <игрок>"));
                } else {
                    sender.sendMessage("[TreexBuyer] ▶ &fИспользование /" + cmd.getName() + " open <id> <игрок>");
                }

            }

        }


        return true;

    }
    private MenuManager menuManager;
}
