package pl.olczku.miniGames.randomizer.game;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import pl.olczku.miniGames.randomizer.config.RandomizerConfig;
import pl.olczku.miniGames.randomizer.gui.RandomizerMenu;
import pl.olczku.miniGames.randomizer.item.RandomItemProvider;
import pl.olczku.miniGames.randomizer.queue.RandomizerQueue;
import pl.olczku.miniGames.randomizer.util.Text;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class RandomizerService {
    private final JavaPlugin plugin;
    private final RandomizerConfig config;
    private final EnumMap<RandomizerMode, RandomizerQueue> queues = new EnumMap<>(RandomizerMode.class);
    private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<>();
    private final Map<UUID, RandomizerMode> membership = new HashMap<>();
    private final RandomItemProvider items;
    private final EnumMap<RandomizerMode, Integer> elapsed = new EnumMap<>(RandomizerMode.class);
    private BukkitTask ticker;
    private RandomizerMenu menu;

    public RandomizerService(JavaPlugin plugin, RandomizerConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.items = new RandomItemProvider(config);
        for (RandomizerMode mode : RandomizerMode.values()) {
            queues.put(mode, new RandomizerQueue(mode));
            elapsed.put(mode, 0);
        }
    }

    public void setMenu(RandomizerMenu menu) { this.menu = menu; }
    public RandomizerQueue queue(RandomizerMode mode) { return queues.get(mode); }
    public RandomizerMode modeOf(UUID id) { return membership.get(id); }
    public void openMenu(Player player) { menu.open(player); }

    public void join(Player player, RandomizerMode mode) {
        if (membership.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Jesteś już w kolejce lub grze.</red>"));
            return;
        }
        RandomizerQueue queue = queue(mode);
        if (!queue.add(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Ta kolejka jest pełna.</red>"));
            return;
        }
        membership.put(player.getUniqueId(), mode);
        player.sendMessage(Text.mm("<green>Dołączono do Randomizera <yellow>" + mode.id() + "</yellow>.</green>"));
        updateTab(queue);
        if (queue.full() && queue.state() == GameState.WAITING) {
            queue.state(GameState.COUNTDOWN);
            queue.countdown(config.countdownSeconds);
        }
    }

    public void leave(Player player) {
        RandomizerMode mode = membership.remove(player.getUniqueId());
        if (mode == null) {
            player.sendMessage(Text.mm("<red>Nie jesteś w Randomizerze.</red>"));
            return;
        }
        RandomizerQueue queue = queue(mode);
        queue.remove(player.getUniqueId());
        PlayerSnapshot snapshot = snapshots.remove(player.getUniqueId());
        if (snapshot != null) snapshot.restore(player);
        player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        player.sendMessage(Text.mm("<yellow>Opuszczono Randomizer.</yellow>"));
        if (queue.state() == GameState.RUNNING) checkWinner(queue);
        else {
            queue.state(GameState.WAITING);
            updateTab(queue);
        }
    }

    public void startTicker() {
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        for (RandomizerQueue queue : queues.values()) {
            switch (queue.state()) {
                case WAITING -> waiting(queue);
                case COUNTDOWN -> countdown(queue);
                case RUNNING -> running(queue);
                default -> { }
            }
        }
    }

    private void waiting(RandomizerQueue queue) {
        updateTab(queue);
        for (Player player : online(queue)) {
            player.sendActionBar(Text.mm("<gold>" + Text.small("oczekiwanie") + "... <gray>" + queue.players().size() + "/" + queue.mode().maxPlayers() + "</gray></gold>"));
        }
    }

    private void countdown(RandomizerQueue queue) {
        if (!queue.full()) {
            queue.state(GameState.WAITING);
            return;
        }
        int seconds = queue.countdown();
        for (Player player : online(queue)) {
            player.sendActionBar(Text.mm("<yellow>" + Text.small("start za") + " <white>" + seconds + "</white></yellow>"));
            if (seconds <= 5 && seconds > 0) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
        }
        if (seconds <= 0) start(queue);
        else queue.countdown(seconds - 1);
        updateTab(queue);
    }

    private void start(RandomizerQueue queue) {
        List<Player> players = online(queue);
        if (players.size() != queue.mode().maxPlayers()) {
            queue.state(GameState.WAITING);
            return;
        }
        List<Location> spawns = spawns(queue.mode());
        if (spawns.size() < players.size()) {
            broadcast(queue, "<red>Brak skonfigurowanych spawnów dla " + queue.mode().id() + ".</red>");
            queue.state(GameState.WAITING);
            return;
        }
        queue.state(GameState.RUNNING);
        elapsed.put(queue.mode(), 0);
        WorldBorder border = Bukkit.createWorldBorder();
        Location center = config.arenaCenter != null ? config.arenaCenter : spawns.getFirst();
        border.setCenter(center);
        border.setSize(config.borderStartSize);
        for (int index = 0; index < players.size(); index++) {
            Player player = players.get(index);
            snapshots.put(player.getUniqueId(), PlayerSnapshot.take(player));
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.teleport(spawns.get(index));
            player.setWorldBorder(border);
            player.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, Text.mm("<gold><bold>RANDOMIZER</bold></gold>"));
        }
        broadcast(queue, "<green>Start! Co minutę dostajesz losowy przedmiot.</green>");
        updateTab(queue);
    }

    private void running(RandomizerQueue queue) {
        int seconds = elapsed.merge(queue.mode(), 1, Integer::sum);
        if (seconds % config.itemIntervalSeconds == 0) {
            for (Player player : alive(queue)) {
                ItemStack item = items.next();
                HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                leftovers.values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
                player.sendMessage(Text.mm("<gold>Wylosowano: <white>" + item.getType() + "</white></gold>"));
                player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1, 1);
            }
        }
        if (seconds == config.borderDelaySeconds) {
            for (Player player : online(queue)) {
                WorldBorder border = player.getWorldBorder();
                if (border != null) border.setSize(config.borderEndSize, Duration.ofSeconds(config.borderShrinkSeconds));
            }
            broadcast(queue, "<red>Border zaczyna się zmniejszać!</red>");
        }
        updateTab(queue);
        checkWinner(queue);
    }

    public void onDeath(Player player) {
        RandomizerMode mode = membership.get(player.getUniqueId());
        if (mode == null) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            checkWinner(queue(mode));
        });
    }

    private void checkWinner(RandomizerQueue queue) {
        if (queue.state() != GameState.RUNNING) return;
        List<Player> alive = alive(queue);
        Set<Integer> teams = new HashSet<>();
        List<UUID> order = new ArrayList<>(queue.players());
        for (Player player : alive) teams.add(order.indexOf(player.getUniqueId()) / queue.mode().teamSize());
        if (teams.size() <= 1) end(queue, alive);
    }

    private void end(RandomizerQueue queue, List<Player> winners) {
        queue.state(GameState.ENDING);
        broadcast(queue, winners.isEmpty()
            ? "<red>Nikt nie wygrał.</red>"
            : "<gold>Wygrywa: <white>" + winners.stream().map(Player::getName).reduce((a, b) -> a + ", " + b).orElse("") + "</white></gold>");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : online(queue)) {
                PlayerSnapshot snapshot = snapshots.remove(player.getUniqueId());
                if (snapshot != null) snapshot.restore(player);
                membership.remove(player.getUniqueId());
                player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            }
            queue.clear();
            elapsed.put(queue.mode(), 0);
        }, 100L);
    }

    private List<Location> spawns(RandomizerMode mode) {
        return switch (mode) {
            case ONE_V_ONE -> config.spawns1v1;
            case TWO_V_TWO -> config.spawns2v2;
            case FOUR_V_FOUR -> config.spawns4v4;
        };
    }

    private List<Player> online(RandomizerQueue queue) {
        return queue.players().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    private List<Player> alive(RandomizerQueue queue) {
        return online(queue).stream().filter(player -> player.getGameMode() != GameMode.SPECTATOR && !player.isDead()).toList();
    }

    private void broadcast(RandomizerQueue queue, String message) {
        online(queue).forEach(player -> player.sendMessage(Text.mm(message)));
    }

    private void updateTab(RandomizerQueue queue) {
        String footer = config.tabFooter
            .replace("{mode}", queue.mode().id())
            .replace("{players}", String.valueOf(queue.players().size()))
            .replace("{max}", String.valueOf(queue.mode().maxPlayers()));
        for (Player player : online(queue)) {
            player.sendPlayerListHeaderAndFooter(Text.mm(config.tabHeader), Text.mm(footer + "\n<gray>Status: <white>" + queue.state() + "</white></gray>"));
        }
    }

    public void shutdown() {
        if (ticker != null) ticker.cancel();
        for (UUID id : new ArrayList<>(membership.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                PlayerSnapshot snapshot = snapshots.get(id);
                if (snapshot != null) snapshot.restore(player);
            }
        }
        membership.clear();
        snapshots.clear();
    }
}
