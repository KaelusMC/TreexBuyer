package me.jetby.treexbuyer.commands;

import me.jetby.treexbuyer.loader.MenuLoader;
import me.jetby.treexbuyer.menuManager.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class PluginCompleter implements TabCompleter {
    private MenuManager menuManager;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            List<String> commands = List.of("open", "reload");

            if (args.length == 1) {
                String input = args[0].toLowerCase();
                return commands.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
                if (player.hasPermission("treexbuyer.admin")) {
                    List<String> menuIds = MenuLoader.getListMenu().keySet().stream()
                            .filter(menuId -> menuId.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                    return menuIds;
                } else {
                    return List.of();
                }
            }
        }

        return List.of();
    }
}