package pl.olczku.miniGames.randomizer.stats;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RandomizerStats {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;
    private final Map<UUID, Entry> cache = new HashMap<>();
    private boolean dirty;
    private int secondsSinceSave;

    public RandomizerStats(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "randomizer-stats.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public void playerSeen(Player player) {
        Entry entry = entry(player.getUniqueId());
        entry.name = player.getName();
        dirty = true;
    }

    public void tickOnline(Collection<? extends Player> players) {
        for (Player player : players) {
            Entry entry = entry(player.getUniqueId());
            entry.name = player.getName();
            entry.playTimeSeconds++;
        }
        if (!players.isEmpty()) dirty = true;

        secondsSinceSave++;
        if (secondsSinceSave >= 60) save();
    }

    public void addKill(Player player) {
        Entry entry = entry(player.getUniqueId());
        entry.name = player.getName();
        entry.kills++;
        dirty = true;
    }

    public void addDeath(Player player) {
        Entry entry = entry(player.getUniqueId());
        entry.name = player.getName();
        entry.deaths++;
        dirty = true;
    }

    public void addWin(Player player) {
        Entry entry = entry(player.getUniqueId());
        entry.name = player.getName();
        entry.wins++;
        dirty = true;
    }

    public long playTimeSeconds(UUID uuid) { return entry(uuid).playTimeSeconds; }
    public int kills(UUID uuid) { return entry(uuid).kills; }
    public int deaths(UUID uuid) { return entry(uuid).deaths; }
    public int wins(UUID uuid) { return entry(uuid).wins; }

    public void save() {
        if (!dirty) {
            secondsSinceSave = 0;
            return;
        }

        for (Map.Entry<UUID, Entry> mapEntry : cache.entrySet()) {
            String path = "players." + mapEntry.getKey();
            Entry entry = mapEntry.getValue();
            yaml.set(path + ".name", entry.name);
            yaml.set(path + ".play-time-seconds", entry.playTimeSeconds);
            yaml.set(path + ".kills", entry.kills);
            yaml.set(path + ".deaths", entry.deaths);
            yaml.set(path + ".wins", entry.wins);
        }

        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            yaml.save(file);
            dirty = false;
            secondsSinceSave = 0;
        } catch (IOException exception) {
            plugin.getLogger().severe("Nie udalo sie zapisac randomizer-stats.yml: " + exception.getMessage());
        }
    }

    private Entry entry(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    private Entry load(UUID uuid) {
        String path = "players." + uuid;
        Entry entry = new Entry();
        entry.name = yaml.getString(path + ".name", uuid.toString());
        entry.playTimeSeconds = Math.max(0L, yaml.getLong(path + ".play-time-seconds", 0L));
        entry.kills = Math.max(0, yaml.getInt(path + ".kills", 0));
        entry.deaths = Math.max(0, yaml.getInt(path + ".deaths", 0));
        entry.wins = Math.max(0, yaml.getInt(path + ".wins", 0));
        return entry;
    }

    private static final class Entry {
        private String name;
        private long playTimeSeconds;
        private int kills;
        private int deaths;
        private int wins;
    }
}
