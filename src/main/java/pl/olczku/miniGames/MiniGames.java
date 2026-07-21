package pl.olczku.miniGames;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.bukkit.SerdesBukkit;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import me.devnatan.inventoryframework.ViewFrame;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import pl.olczku.miniGames.randomizer.command.RandomizerCommand;
import pl.olczku.miniGames.randomizer.config.RandomizerConfig;
import pl.olczku.miniGames.randomizer.game.RandomizerService;
import pl.olczku.miniGames.randomizer.gui.RandomizerMenu;
import pl.olczku.miniGames.randomizer.listener.RandomizerListener;

import java.io.File;

public final class MiniGames extends JavaPlugin {
    private LiteCommands<CommandSender> commands;
    private ViewFrame viewFrame;
    private RandomizerService randomizer;

    @Override
    public void onEnable() {
        RandomizerConfig config = ConfigManager.create(RandomizerConfig.class, it -> {
            it.configure(opt -> {
                opt.configurer(new YamlBukkitConfigurer(), new SerdesBukkit());
                opt.bindFile(new File(getDataFolder(), "randomizer.yml"));
                opt.removeOrphans(false);
            });
            it.saveDefaults();
            it.load(true);
        });

        this.randomizer = new RandomizerService(this, config);
        RandomizerMenu menu = new RandomizerMenu(randomizer);
        this.viewFrame = ViewFrame.create(this).with(menu).register();
        this.randomizer.setMenu(menu);

        this.commands = LiteBukkitFactory.builder("minigames")
            .commands(new RandomizerCommand(randomizer))
            .build();

        getServer().getPluginManager().registerEvents(new RandomizerListener(randomizer), this);
        randomizer.startTicker();
        getLogger().info("Randomizer uruchomiony. GUI: Inventory Framework, config: Okaeri, komendy: LiteCommands.");
    }

    @Override
    public void onDisable() {
        if (commands != null) commands.unregister();
        if (viewFrame != null) viewFrame.unregister();
        if (randomizer != null) randomizer.shutdown();
    }
}
