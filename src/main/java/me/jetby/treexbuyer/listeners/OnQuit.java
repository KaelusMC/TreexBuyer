package me.jetby.treexbuyer.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static me.jetby.treexbuyer.autoBuy.AutoBuy.savePlayerScoreAsync;


public class OnQuit implements Listener {

    @EventHandler
    public void PlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();

        savePlayerScoreAsync(playerId);
    }
}
