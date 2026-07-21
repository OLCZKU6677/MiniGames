package pl.olczku.miniGames.randomizer.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class RandomizerConfig {
    public int countdownSeconds;
    public int itemIntervalSeconds;
    public int borderDelaySeconds;
    public double borderStartSize;
    public double borderEndSize;
    public int borderShrinkSeconds;
    public String tabHeader;
    public String tabFooter;
    public List<String> vanillaItems;
    public List<String> nexoItems;
    public Location lobby;
    public Location arenaCenter;
    public List<Location> spawns1v1;
    public List<Location> spawns2v2;
    public List<Location> spawns4v4;

    public static RandomizerConfig load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "randomizer.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        yaml.addDefault("countdown-seconds", 10);
        yaml.addDefault("item-interval-seconds", 60);
        yaml.addDefault("border.delay-seconds", 180);
        yaml.addDefault("border.start-size", 120.0);
        yaml.addDefault("border.end-size", 8.0);
        yaml.addDefault("border.shrink-seconds", 180);
        yaml.addDefault("tab.header", "<gradient:#ff7a00:#ffcc55><bold>RANDOMIZER</bold></gradient>");
        yaml.addDefault("tab.footer", "<gray>Tryb: <yellow>{mode}</yellow>  •  Gracze: <white>{players}/{max}</white></gray>");
        yaml.addDefault("items.vanilla", List.of("DIAMOND_SWORD", "BOW", "CROSSBOW", "GOLDEN_APPLE", "ENDER_PEARL", "SHIELD", "TRIDENT", "COBWEB", "LAVA_BUCKET", "WATER_BUCKET", "TNT", "FLINT_AND_STEEL"));
        yaml.addDefault("items.nexo", List.of());
        yaml.addDefault("lobby", null);
        yaml.addDefault("arena-center", null);
        yaml.addDefault("spawns.1v1", List.of());
        yaml.addDefault("spawns.2v2", List.of());
        yaml.addDefault("spawns.4v4", List.of());
        yaml.options().copyDefaults(true);

        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Nie udało się zapisać randomizer.yml: " + exception.getMessage());
        }

        RandomizerConfig config = new RandomizerConfig();
        config.countdownSeconds = Math.max(1, yaml.getInt("countdown-seconds"));
        config.itemIntervalSeconds = Math.max(1, yaml.getInt("item-interval-seconds"));
        config.borderDelaySeconds = Math.max(0, yaml.getInt("border.delay-seconds"));
        config.borderStartSize = Math.max(1.0, yaml.getDouble("border.start-size"));
        config.borderEndSize = Math.max(1.0, yaml.getDouble("border.end-size"));
        config.borderShrinkSeconds = Math.max(1, yaml.getInt("border.shrink-seconds"));
        config.tabHeader = yaml.getString("tab.header", "<gold><bold>RANDOMIZER</bold></gold>");
        config.tabFooter = yaml.getString("tab.footer", "<gray>{mode} {players}/{max}</gray>");
        config.vanillaItems = new ArrayList<>(yaml.getStringList("items.vanilla"));
        config.nexoItems = new ArrayList<>(yaml.getStringList("items.nexo"));
        config.lobby = yaml.getLocation("lobby");
        config.arenaCenter = yaml.getLocation("arena-center");
        config.spawns1v1 = locations(yaml.getList("spawns.1v1"));
        config.spawns2v2 = locations(yaml.getList("spawns.2v2"));
        config.spawns4v4 = locations(yaml.getList("spawns.4v4"));
        return config;
    }

    private static List<Location> locations(List<?> values) {
        List<Location> result = new ArrayList<>();
        if (values == null) return result;
        for (Object value : values) {
            if (value instanceof Location location) result.add(location);
        }
        return result;
    }
}
