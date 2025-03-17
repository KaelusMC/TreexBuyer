package me.jetby.treexbuyer.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.treexbuyer.Main;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ASellerPlaceholder extends PlaceholderExpansion {

    private final Main plugin;

    public ASellerPlaceholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return null;
    }
    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    @Override
    public String getIdentifier() {

        return "buyer";
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (identifier.startsWith("pay")) {


        }
            return null;
        }
}
