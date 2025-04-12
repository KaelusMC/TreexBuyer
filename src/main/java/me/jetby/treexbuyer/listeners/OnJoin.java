package me.jetby.treexbuyer.listeners;

import me.jetby.treexbuyer.autoBuy.AutoBuy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;


public class OnJoin implements Listener {

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!AutoBuy.getAutoBuyItemsMap().containsKey(playerId)) {
            AutoBuy.checkPlayer(playerId);
        }
    }
}
