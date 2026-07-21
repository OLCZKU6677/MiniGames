package pl.olczku.miniGames;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.olczku.miniGames.randomizer.command.RandomizerCommand;
import pl.olczku.miniGames.randomizer.config.RandomizerConfig;
import pl.olczku.miniGames.randomizer.game.RandomizerService;
import pl.olczku.miniGames.randomizer.gui.RandomizerMenu;
import pl.olczku.miniGames.randomizer.listener.RandomizerListener;

public final class MiniGames extends JavaPlugin {
    private RandomizerService randomizer;

    @Override
    public void onEnable() {
        RandomizerConfig config = RandomizerConfig.load(this);
        this.randomizer = new RandomizerService(this, config);

        RandomizerMenu menu = new RandomizerMenu(randomizer);
        randomizer.setMenu(menu);

        RandomizerCommand commandExecutor = new RandomizerCommand(randomizer);
        PluginCommand command = getCommand("randomizer");
        if (command == null) {
            throw new IllegalStateException("Brak komendy randomizer w plugin.yml");
        }
        command.setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);

        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(new RandomizerListener(randomizer), this);
        randomizer.startTicker();
        getLogger().info("Randomizer został uruchomiony.");
    }

    @Override
    public void onDisable() {
        if (randomizer != null) randomizer.shutdown();
    }
}
