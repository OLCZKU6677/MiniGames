package pl.olczku.miniGames.randomizer.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.olczku.miniGames.randomizer.game.RandomizerService;

public final class RandomizerListener implements Listener {
    private final RandomizerService service;

    public RandomizerListener(RandomizerService service) {
        this.service = service;
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        if (service.modeOf(event.getEntity().getUniqueId()) == null) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        service.onDeath(event.getEntity());
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        if (service.modeOf(event.getPlayer().getUniqueId()) != null) {
            service.leave(event.getPlayer());
        }
    }
}
