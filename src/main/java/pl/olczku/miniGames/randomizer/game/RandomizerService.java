package pl.olczku.miniGames.randomizer.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import pl.olczku.miniGames.randomizer.config.RandomizerConfig;
import pl.olczku.miniGames.randomizer.gui.RandomizerMenu;
import pl.olczku.miniGames.randomizer.item.RandomItemProvider;
import pl.olczku.miniGames.randomizer.party.PartyManager;
import pl.olczku.miniGames.randomizer.queue.RandomizerQueue;
import pl.olczku.miniGames.randomizer.stats.RandomizerStats;
import pl.olczku.miniGames.randomizer.util.Text;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomizerService {
    private final JavaPlugin plugin;
    private final RandomizerConfig config;
    private final List<RandomizerQueue> arenas = new ArrayList<>();
    private final List<WorldSlot> worldSlots = new ArrayList<>();
    private final List<PreparedArena> preparedArenas = new ArrayList<>();
    private final Map<UUID, RandomizerQueue> membership = new HashMap<>();
    private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Scoreboard> previousScoreboards = new HashMap<>();
    private final Set<UUID> eliminated = new HashSet<>();
    private final Map<UUID, Long> separationCooldown = new HashMap<>();
    private final RandomItemProvider items;
    private final PartyManager parties;
    private final RandomizerStats stats;
    private final NamespacedKey leaveItemKey;
    private BukkitTask ticker;
    private BukkitTask lobbyVisualTicker;
    private BukkitTask borderPushTicker;
    private RandomizerMenu menu;

    public RandomizerService(JavaPlugin plugin, RandomizerConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.items = new RandomItemProvider(config);
        this.parties = new PartyManager(this);
        this.stats = new RandomizerStats(plugin);
        this.leaveItemKey = new NamespacedKey(plugin, "randomizer_leave");
    }

    public void setMenu(RandomizerMenu menu) { this.menu = menu; }
    public RandomizerConfig config() { return config; }
    public void openMenu(Player player) { menu.open(player); }
    public RandomizerMode modeOf(UUID id) {
        RandomizerQueue queue = membership.get(id);
        return queue == null ? null : queue.mode();
    }


    public boolean isRandomizerWorld(World world) {
        if (world == null) return false;
        return worldSlots.stream().anyMatch(slot -> slot.world.equals(world));
    }

    public boolean isPreGame(Player player) {
        RandomizerQueue queue = membership.get(player.getUniqueId());
        return queue != null && (queue.state() == GameState.WAITING || queue.state() == GameState.COUNTDOWN);
    }

    /**
     * Pewnie odpycha gracza od wirtualnego bordera do srodka areny.
     * Sam Player#setWorldBorder wyswietla border, ale nie zawsze zapewnia fizyczne odbicie,
     * dlatego pozycja jest dodatkowo korygowana i dopiero potem nadawana jest predkosc.
     */
    public void pushApartOnArena(Player player) {
        pushApartOnArena(player, player.getLocation());
    }


    public void pushApartOnArena(Player player, Location attemptedLocation) {
        RandomizerQueue queue = membership.get(player.getUniqueId());
        if (queue == null || queue.session() == null || queue.state() == GameState.ENDING) return;
        if (!player.isOnline() || eliminated.contains(player.getUniqueId())
                || player.getGameMode() == GameMode.SPECTATOR || attemptedLocation == null) return;

        WorldBorder border = queue.session().border();
        Location center = queue.session().center();
        if (attemptedLocation.getWorld() == null
                || !attemptedLocation.getWorld().equals(queue.session().world())) return;

        // Na poczekalni odbijamy od 1 bloku, a w odliczaniu i grze od 3 blokow.
        // Nie teleportujemy gracza - cala korekta odbywa sie przez plynna animacje velocity.
        double trigger = queue.state() == GameState.WAITING ? 1.0D : 3.0D;
        double half = border.getSize() / 2.0D;
        double minX = center.getX() - half;
        double maxX = center.getX() + half;
        double minZ = center.getZ() - half;
        double maxZ = center.getZ() + half;

        double x = attemptedLocation.getX();
        double z = attemptedLocation.getZ();
        double west = x - minX;
        double east = maxX - x;
        double north = z - minZ;
        double south = maxZ - z;
        double nearest = Math.min(Math.min(west, east), Math.min(north, south));
        if (nearest > trigger) return;

        long now = System.currentTimeMillis();
        if (separationCooldown.getOrDefault(player.getUniqueId(), 0L) > now) return;
        separationCooldown.put(player.getUniqueId(), now + 650L);

        Vector direction = new Vector(0.0D, 0.0D, 0.0D);
        if (west <= east && west <= north && west <= south) {
            direction.setX(1.0D);
        } else if (east <= west && east <= north && east <= south) {
            direction.setX(-1.0D);
        } else if (north <= west && north <= east && north <= south) {
            direction.setZ(1.0D);
        } else {
            direction.setZ(-1.0D);
        }

        // Gdy gracz jest przy rogu, dodajemy skladowa w strone srodka,
        // aby nie przesuwal sie wzdluz drugiej sciany bordera.
        Vector towardCenter = center.toVector().subtract(attemptedLocation.toVector()).setY(0.0D);
        if (towardCenter.lengthSquared() > 0.0001D) {
            towardCenter.normalize().multiply(0.35D);
            direction.add(towardCenter).normalize();
        }

        final Vector baseDirection = direction.clone();
        final double[] strengths = {0.48D, 0.34D, 0.22D, 0.12D};

        // Zachowujemy wcześniejszą, pewną detekcję bordera i płynną animację,
        // ale łączny impuls jest ograniczony do około jednego bloku.
        for (int tick = 0; tick < strengths.length; tick++) {
            final int step = tick;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                RandomizerQueue current = membership.get(player.getUniqueId());
                if (current != queue || current.state() == GameState.ENDING
                        || player.getGameMode() == GameMode.SPECTATOR) return;

                double upward = step == 0 ? 0.12D : Math.max(0.02D, player.getVelocity().getY());
                Vector animatedPush = baseDirection.clone().multiply(strengths[step]).setY(upward);

                // Zostawiamy minimalną część aktualnego ruchu, aby odbicie nadal wyglądało
                // naturalnie, ale nie rozpędzało gracza dalej niż około jeden blok.
                animatedPush.add(player.getVelocity().multiply(0.08D));
                player.setVelocity(animatedPush);
            }, tick);
        }
    }

    public int queuedPlayers(RandomizerMode mode) {
        return arenas.stream()
            .filter(queue -> queue.mode() == mode)
            .filter(queue -> queue.state() == GameState.WAITING || queue.state() == GameState.COUNTDOWN)
            .mapToInt(queue -> queue.players().size())
            .max().orElse(0);
    }

    public void handlePartyCommand(Player player, String[] args) { parties.handle(player, args); }
    public void handlePartyQuit(Player player) { parties.onQuit(player); }

    public void join(Player player, RandomizerMode mode) {
        if (!parties.validateMode(player, mode)) return;
        List<Player> joining = parties.onlineParty(player);
        if (joining.isEmpty()) return;
        if (joining.stream().anyMatch(member -> membership.containsKey(member.getUniqueId()))) {
            player.sendMessage(Text.mm(config.msgAlreadyPlaying));
            return;
        }
        joinGroup(joining, mode);
    }

    private void joinGroup(List<Player> joining, RandomizerMode mode) {
        joinGroup(joining, mode, 0);
    }

    /**
     * Jeżeli serwer właśnie uruchomił świat Randomizera, arena może potrzebować
     * kilku sekund na asynchroniczne przygotowanie rdzenia chunków. Zamiast
     * błędnie zwracać „brak wolnej areny”, dołączenie jest wtedy ponawiane.
     */
    private void joinGroup(List<Player> joining, RandomizerMode mode, int arenaWaitAttempt) {
        joining = joining.stream().filter(Player::isOnline).toList();
        if (joining.isEmpty()) return;

        Player player = joining.get(0);
        if (membership.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.mm(config.msgAlreadyPlaying));
            return;
        }

        RandomizerQueue queue = findJoinable(mode);
        if (queue == null) {
            ArenaSession session = allocateArena();
            if (session == null) {
                boolean preparing = config.preloadArenaChunks
                    && preparedArenas.stream().anyMatch(arena -> !arena.ready && !arena.inUse);
                if (preparing && arenaWaitAttempt < 45) {
                    List<Player> retryPlayers = new ArrayList<>(joining);
                    Bukkit.getScheduler().runTaskLater(plugin,
                        () -> joinGroup(retryPlayers, mode, arenaWaitAttempt + 1), 20L);
                    return;
                }
                player.sendMessage(Text.mm(config.msgNoArena));
                return;
            }
            queue = new RandomizerQueue(mode, session);
            arenas.add(queue);
        }

        if (!queue.canFit(joining.size())) {
            player.sendMessage(Text.mm(config.msgQueueFull));
            return;
        }

        for (Player member : joining) {
            if (!queue.add(member.getUniqueId())) continue;
            membership.put(member.getUniqueId(), queue);
            snapshots.put(member.getUniqueId(), PlayerSnapshot.take(member));
            previousScoreboards.put(member.getUniqueId(), member.getScoreboard());
            kills.put(member.getUniqueId(), 0);
            eliminated.remove(member.getUniqueId());
            prepareWaitingPlayer(member, queue);
            Map<String, String> ph = placeholders(queue, member);
            ph.put("game", String.format("%02d", queue.session().slot() + 1));
            member.sendMessage(Text.mm(config.msgJoin, ph));
            member.playSound(member, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
            showWaitingTitle(member, queue);
        }
        updateVisuals(queue);
        refreshPlayerVisibility();

        if (queue.readyToStart() && queue.state() == GameState.WAITING) {
            queue.state(GameState.COUNTDOWN);
            queue.countdown(config.countdownSeconds);
            warmupFightChunks(queue);
        }
    }

    public void leave(Player player) {
        RandomizerQueue queue = membership.remove(player.getUniqueId());
        separationCooldown.remove(player.getUniqueId());
        if (queue == null) {
            player.sendMessage(Text.mm(config.msgNotPlaying));
            return;
        }

        queue.remove(player.getUniqueId());
        eliminated.remove(player.getUniqueId());
        restorePlayer(player);
        updateLobbyVisuals(player);
        refreshPlayerVisibility();
        player.sendMessage(Text.mm(config.msgLeave));

        if (queue.state() == GameState.RUNNING) {
            checkWinner(queue);
        } else if (queue.players().isEmpty()) {
            removeArena(queue);
        } else {
            queue.state(GameState.WAITING);
            queue.countdown(0);
            for (Player member : online(queue)) showWaitingTitle(member, queue);
            updateVisuals(queue);
        }
    }

    public void startTicker() {
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
        // Osobny, częstszy task utrzymuje TAB i scoreboard na spawnie także wtedy,
        // gdy inny plugin ustawia własny scoreboard po wejściu lub zmianie świata.
        lobbyVisualTicker = Bukkit.getScheduler().runTaskTimer(plugin, () -> updateLobbyVisuals(), 5L, 20L);
        // Sprawdzanie co 2 ticki gwarantuje odbicie nawet wtedy, gdy PlayerMoveEvent
        // nie zostanie wywolany przez lag, teleport lub ruch samego bordera.
        borderPushTicker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new ArrayList<>(membership.keySet())) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) pushApartOnArena(player);
            }
        }, 2L, 2L);
        initializeWorlds();
    }

    /**
     * Randomizer używa jednego wspólnego świata i tworzy w nim wiele oddalonych aren.
     * Dzięki temu serwer nie generuje przy starcie kilku osobnych wymiarów.
     */
    private void initializeWorlds() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> loadWorldSlot(0), 20L);
    }

    private void loadWorldSlot(int index) {
        if (worldSlots.stream().anyMatch(slot -> slot.index == index)) return;

        String name = "randomizer_world_" + (index + 1);
        World world = Bukkit.getWorld(name);
        if (world == null) {
            world = new WorldCreator(name)
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .biomeProvider(new PlainsBiomeProvider())
                .createWorld();
        }
        if (world == null) {
            plugin.getLogger().severe("Nie udało się utworzyć świata " + name);
            return;
        }

        world.setAutoSave(false);
        setWorldPvp(world, true);
        // Ustawiamy pogodę i czas tylko raz podczas ładowania świata.
        // Listener blokuje późniejsze rozpoczęcie deszczu/burzy, więc nie ma migania nieba.
        world.setStorm(false);
        world.setThundering(false);
        world.setClearWeatherDuration(Integer.MAX_VALUE);
        world.setTime(6000L);
        setBooleanGameRuleCompat(world, "doDaylightCycle", false);
        setBooleanGameRuleCompat(world, "doWeatherCycle", false);
        setBooleanGameRuleCompat(world, "doMobSpawning", false);
        worldSlots.add(new WorldSlot(index, world));
        if (config.preloadArenaChunks) {
            prepareArenaPool(index, world);
        }
    }

    /**
     * Rezerwuje centra aren podczas startu pluginu i doczytuje ich chunki stopniowo.
     * Dzięki temu pierwsza teleportacja nie generuje terenu na głównym ticku gry.
     */
    private void prepareArenaPool(int slotIndex, World world) {
        for (int i = 0; i < config.preloadArenaCount; i++) {
            Location center = reserveArenaCenter(world);
            if (center == null) {
                plugin.getLogger().warning("Nie znaleziono miejsca dla preładowanej areny " + (i + 1) + ".");
                continue;
            }

            PreparedArena prepared = new PreparedArena(slotIndex, world, center);
            preparedArenas.add(prepared);
            preloadChunksGradually(prepared);
        }
    }

    /**
     * Rezerwuje współrzędne bez synchronicznego generowania chunków. Wcześniej
     * findSafeArenaCenter() wywoływało getChunkAt(...).load(true) na głównym
     * wątku, co potrafiło zatrzymać serwer i pozostawiało pulę aren pustą.
     */
    private Location reserveArenaCenter(World world) {
        for (int attempt = 0; attempt < 64; attempt++) {
            int x = randomArenaCoordinate();
            int z = randomArenaCoordinate();
            Location candidate = new Location(world, x + 0.5, 80.0, z + 0.5);
            boolean tooClose = arenas.stream().map(RandomizerQueue::session)
                .filter(session -> session.world().equals(world))
                .anyMatch(session -> horizontalDistanceSquared(session.center(), candidate) < 900_000.0);
            if (!tooClose) {
                tooClose = preparedArenas.stream().filter(arena -> arena.world.equals(world))
                    .anyMatch(arena -> horizontalDistanceSquared(arena.center, candidate) < 900_000.0);
            }
            if (!tooClose) return candidate;
        }
        return null;
    }

    private double horizontalDistanceSquared(Location first, Location second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    private void preloadChunksGradually(PreparedArena prepared) {
        int radiusBlocks = Math.max(config.terrainSize, (int) Math.ceil(config.borderGameSize)) / 2 + 32;
        int centerChunkX = prepared.center.getBlockX() >> 4;
        int centerChunkZ = prepared.center.getBlockZ() >> 4;
        int minChunkX = ((int) Math.floor(prepared.center.getX() - radiusBlocks)) >> 4;
        int maxChunkX = ((int) Math.floor(prepared.center.getX() + radiusBlocks)) >> 4;
        int minChunkZ = ((int) Math.floor(prepared.center.getZ() - radiusBlocks)) >> 4;
        int maxChunkZ = ((int) Math.floor(prepared.center.getZ() + radiusBlocks)) >> 4;

        List<Long> chunks = new ArrayList<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(chunkKey(chunkX, chunkZ));
            }
        }
        // Najpierw środek areny, żeby kolejka była dostępna po kilku sekundach,
        // a dalsze chunki mogły dogrywać się już w tle.
        chunks.sort(Comparator.comparingInt(key -> {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) (long) key;
            int dx = chunkX - centerChunkX;
            int dz = chunkZ - centerChunkZ;
            return dx * dx + dz * dz;
        }));

        AtomicInteger next = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();
        AtomicInteger coreCompleted = new AtomicInteger();
        int total = chunks.size();
        int coreRadius = 3; // 7x7 chunków wystarcza do znalezienia bezpiecznego środka w promieniu 48 bloków.
        int coreTotal = (coreRadius * 2 + 1) * (coreRadius * 2 + 1);
        final BukkitTask[] task = new BukkitTask[1];
        task[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int submitted = 0;
            while (submitted < Math.max(config.preloadChunksPerTick, prepared.ready ? 1 : 8)) {
                int index = next.getAndIncrement();
                if (index >= total) break;
                long key = chunks.get(index);
                int chunkX = (int) (key >> 32);
                int chunkZ = (int) key;
                submitted++;

                prepared.world.getChunkAtAsync(chunkX, chunkZ, true).thenAccept(chunk ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!plugin.isEnabled()) return;
                        if (config.keepArenaChunksLoaded) {
                            chunk.setForceLoaded(true);
                            prepared.forcedChunks.add(key);
                        }

                        if (Math.abs(chunkX - centerChunkX) <= coreRadius
                            && Math.abs(chunkZ - centerChunkZ) <= coreRadius
                            && coreCompleted.incrementAndGet() == coreTotal) {
                            Location safe = safeSurface(prepared.world,
                                prepared.center.getBlockX(), prepared.center.getBlockZ(), 48);
                            if (safe == null) {
                                safe = new Location(prepared.world,
                                    prepared.center.getX(),
                                    prepared.world.getHighestBlockYAt(prepared.center.getBlockX(), prepared.center.getBlockZ()) + 1.0,
                                    prepared.center.getZ());
                            }
                            prepared.center = safe;
                            prepared.ready = true;
                            plugin.getLogger().info("Arena jest gotowa do gry: " + prepared.world.getName() + ".");
                        }

                        if (completed.incrementAndGet() == total) {
                            plugin.getLogger().info("Zakończono doczytywanie całej areny: " + prepared.world.getName()
                                + " (" + total + " chunków).");
                        }
                    })
                ).exceptionally(error -> {
                    plugin.getLogger().warning("Nie udało się przygotować chunka " + chunkX + ", " + chunkZ
                        + ": " + error.getClass().getSimpleName());
                    return null;
                });
            }
            if (next.get() >= total && task[0] != null) task[0].cancel();
        }, 1L, 1L);
    }

    private long chunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
    }


    private void setBooleanGameRuleCompat(World world, String ruleName, boolean value) {
        try {
            World.class
                .getMethod("setGameRuleValue", String.class, String.class)
                .invoke(world, ruleName, Boolean.toString(value));
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Nie udalo sie ustawic gamerule " + ruleName + " w swiecie " + world.getName() + ".");
        }
    }


    /**
     * Wywolanie przez refleksje utrzymuje zgodnosc binarna z Paper 1.21.4,
     * a jednoczesnie nie odwoluje sie bezposrednio do metod oznaczonych
     * w nowszym API jako deprecated/forRemoval.
     */
    private void setBorderWarningTime(WorldBorder border, int seconds) {
        try {
            border.getClass().getMethod("setWarningTime", int.class).invoke(border, Math.max(0, seconds));
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Nie udalo sie ustawic czasu ostrzezenia bordera: " + exception.getClass().getSimpleName());
        }
    }

    private void setWorldPvp(World world, boolean enabled) {
        try {
            world.getClass().getMethod("setPVP", boolean.class).invoke(world, enabled);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Nie udalo sie ustawic PvP w swiecie " + world.getName() + ": " + exception.getClass().getSimpleName());
        }
    }

    private ArenaSession allocateArena() {
        if (worldSlots.isEmpty()) return null;

        PreparedArena prepared = preparedArenas.stream()
            .filter(arena -> arena.ready && !arena.inUse)
            .findFirst().orElse(null);

        WorldSlot slot;
        Location center;
        if (prepared != null) {
            prepared.inUse = true;
            slot = worldSlots.stream().filter(candidate -> candidate.index == prepared.slotIndex).findFirst().orElse(null);
            if (slot == null) return null;
            center = prepared.center.clone();
        } else if (config.preloadArenaChunks) {
            // Nie generujemy terenu podczas dołączania gracza. Arena stanie się dostępna,
            // gdy jej stopniowe przygotowanie przy starcie zostanie zakończone.
            return null;
        } else {
            slot = worldSlots.get(ThreadLocalRandom.current().nextInt(worldSlots.size()));
            center = findSafeArenaCenter(slot.world);
            if (center == null) return null;
            slot.world.getChunkAt(center).load(true);
        }
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(center);
        border.setSize(config.borderWaitingSize);
        // Paper 1.21.4 nie posiada setWarningTimeTicks(int).
        // Ustawienia ostrzezenia sa celowo oparte tylko na stabilnym API.
        border.setWarningDistance(3);
        setBorderWarningTime(border, 15);
        border.setDamageAmount(1.0);
        return new ArenaSession(slot.index, slot.world, center, border);
    }


    private Location findSafeArenaCenter(World world) {
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = randomArenaCoordinate();
            int z = randomArenaCoordinate();
            Location horizontal = new Location(world, x + 0.5, 0.0, z + 0.5);
            boolean tooClose = arenas.stream().map(RandomizerQueue::session).filter(session -> session.world().equals(world)).anyMatch(session -> {
                double dx=session.center().getX()-horizontal.getX(), dz=session.center().getZ()-horizontal.getZ();
                return dx*dx+dz*dz < 900_000.0;
            });
            if (!tooClose) {
                tooClose = preparedArenas.stream().filter(arena -> arena.world.equals(world)).anyMatch(arena -> {
                    double dx = arena.center.getX() - horizontal.getX();
                    double dz = arena.center.getZ() - horizontal.getZ();
                    return dx * dx + dz * dz < 900_000.0;
                });
            }
            if (tooClose) continue;
            world.getChunkAt(x >> 4, z >> 4).load(true);
            Location safe = safeSurface(world, x, z, 48);
            if (safe != null) return safe;
        }
        return null;
    }

    private Location safeSurface(World world, int centerX, int centerZ, int radius) {
        for (int r = 0; r <= radius; r += 4) {
            for (int dx = -r; dx <= r; dx += Math.max(1, r == 0 ? 1 : 4)) {
                for (int dz : new int[]{-r, r}) { Location loc = safeColumn(world, centerX + dx, centerZ + dz); if (loc != null) return loc; }
            }
            for (int dz = -r; dz <= r; dz += Math.max(1, r == 0 ? 1 : 4)) {
                for (int dx : new int[]{-r, r}) { Location loc = safeColumn(world, centerX + dx, centerZ + dz); if (loc != null) return loc; }
            }
        }
        return null;
    }

    private Location safeColumn(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        Material floor = world.getBlockAt(x, y, z).getType();
        if (floor.isAir() || floor == Material.WATER || floor == Material.LAVA || floor.name().contains("KELP") || floor.name().contains("SEAGRASS")) return null;
        if (!world.getBlockAt(x, y + 1, z).isPassable() || !world.getBlockAt(x, y + 2, z).isPassable()) return null;
        return new Location(world, x + 0.5, y + 1.0, z + 0.5);
    }

    private int randomArenaCoordinate() {
        int distance = ThreadLocalRandom.current().nextInt(config.arenaMinDistance, config.arenaMaxDistance + 1);
        return ThreadLocalRandom.current().nextBoolean() ? distance : -distance;
    }

    /**
     * Podczas odliczania Paper generuje w tle chunki, do których gracze zostaną
     * przeniesieni na starcie. Dzięki temu sam teleport nie zatrzymuje ticka serwera.
     */
    private void warmupFightChunks(RandomizerQueue queue) {
        ArenaSession session = queue.session();
        int count = queue.mode().dynamicStart() ? Math.max(2, queue.players().size()) : queue.mode().maxPlayers();
        double radius = Math.max(35.0, Math.min(config.borderGameSize * 0.38, 120.0));

        Set<Long> requested = new HashSet<>();
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 * i) / count;
            int x = (int) Math.round(session.center().getX() + Math.cos(angle) * radius);
            int z = (int) Math.round(session.center().getZ() + Math.sin(angle) * radius);
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            long key = (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
            if (requested.add(key)) session.world().getChunkAtAsync(chunkX, chunkZ, true);
        }
    }

    private void tick() {
        stats.tickOnline(Bukkit.getOnlinePlayers());
        for (RandomizerQueue queue : new ArrayList<>(arenas)) {
            switch (queue.state()) {
                case WAITING -> waiting(queue);
                case COUNTDOWN -> countdown(queue);
                case RUNNING -> running(queue);
                case ENDING -> updateVisuals(queue);
            }
        }
        updateLobbyVisuals();
    }

    private void waiting(RandomizerQueue queue) {
        updateVisuals(queue);
        for (Player player : online(queue)) {
            player.sendActionBar(Component.empty());
        }
    }

    private void showWaitingTitle(Player player, RandomizerQueue queue) {
        Map<String, String> ph = placeholders(queue, player);
        int missing = Math.max(0, queue.mode().maxPlayers() - queue.players().size());
        ph.put("missing", String.valueOf(missing));
        ph.put("player-word", playerWord(missing));
        player.showTitle(Title.title(
            Text.mm(config.titleWaiting, ph),
            Text.mm(config.subtitleWaiting, ph),
            Title.Times.times(Duration.ofMillis(100), Duration.ofDays(7), Duration.ofMillis(100))
        ));
    }

    private void countdown(RandomizerQueue queue) {
        if (!queue.readyToStart()) {
            queue.state(GameState.WAITING);
            queue.countdown(0);
            for (Player player : online(queue)) showWaitingTitle(player, queue);
            updateVisuals(queue);
            return;
        }

        int seconds = queue.countdown();
        Map<String, String> ph = placeholders(queue, null);
        ph.put("seconds", String.valueOf(seconds));
        for (Player player : online(queue)) {
            player.showTitle(Title.title(
                Text.mm(config.titleCountdown, ph),
                Text.mm(config.subtitleCountdown, ph),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ZERO)
            ));
            if (seconds <= 5 && seconds > 0) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1.2f);
        }

        if (seconds <= 0) start(queue);
        else queue.countdown(seconds - 1);
        updateVisuals(queue);
    }

    private void start(RandomizerQueue queue) {
        if (!queue.mode().canStart(online(queue).size())) {
            queue.state(GameState.WAITING);
            return;
        }

        queue.state(GameState.RUNNING);
        queue.resetElapsed();
        queue.session().border().setSize(config.borderGameSize);
        List<Player> players = online(queue);
        List<Location> spawns = generatedSpawns(queue.session(), players.size());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            prepareFightingPlayer(player);
            player.teleport(spawns.get(i));
            player.setRespawnLocation(queue.session().center(), true);
            player.setWorldBorder(queue.session().border());
            player.showTitle(Title.title(
                Text.mm(config.titleStart, placeholders(queue, player)),
                Text.mm(config.subtitleStart, placeholders(queue, player)),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(500))
            ));
        }
        // Pierwsze losowanie odbywa się natychmiast po rozpoczęciu gry.
        giveRandomItems(queue, 1);
        broadcast(queue, config.msgGameStarted, Map.of());
        broadcast(queue, config.msgGoodLuck, Map.of());
        updateVisuals(queue);
    }

    private void giveRandomItems(RandomizerQueue queue, int drawNumber) {
        boolean priorityDrop = drawNumber % 3 == 0;
        for (Player player : alive(queue)) {
            ItemStack item = items.next(player.getUniqueId(), priorityDrop);
            // Nadmiar nie wypada na ziemię. Dzięki temu nie da się obchodzić blokady podnoszenia.
            player.getInventory().addItem(item);
            Map<String, String> ph = placeholders(queue, player);
            ph.put("item", readableItem(item));
            player.sendActionBar(Text.mm(config.actionbarItemReceived, ph));
            player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        }
    }

    private void running(RandomizerQueue queue) {
        int elapsed = queue.tickElapsed();
        if (elapsed % config.itemIntervalSeconds == 0) {
            int drawNumber = 1 + Math.max(1, elapsed / Math.max(1, config.itemIntervalSeconds));
            giveRandomItems(queue, drawNumber);
        } else {
            int remaining = config.itemIntervalSeconds - (elapsed % config.itemIntervalSeconds);
            for (Player player : online(queue)) {
                if (eliminated.contains(player.getUniqueId()) || player.getGameMode() == GameMode.SPECTATOR) {
                    player.sendActionBar(Text.mm(config.actionbarSpectator, placeholders(queue, player)));
                } else {
                    Map<String, String> ph = placeholders(queue, player);
                    ph.put("seconds", String.valueOf(remaining));
                    player.sendActionBar(Text.mm(config.actionbarItemCountdown, ph));
                }
            }
        }

        if (elapsed >= config.borderDelaySeconds && !queue.borderShrinkStarted()) {
            queue.borderShrinkStarted(true);
            startSmoothBorderShrink(
                queue.session().border(),
                config.borderEndSize,
                Math.max(1, config.borderShrinkSeconds)
            );
            if (config.msgBorderShrink != null && !config.msgBorderShrink.isBlank()) {
                broadcast(queue, config.msgBorderShrink, Map.of());
            }
        }

        if (elapsed >= config.gameDurationSeconds) {
            end(queue, alive(queue));
            return;
        }

        updateVisuals(queue);
        checkWinner(queue);
    }

    /**
     * Uruchamia natywne, plynne zmniejszanie bordera po stronie serwera i klienta.
     * Uzywamy refleksji, aby ten sam kod dzialal na Paper 1.21.4 i nowszych
     * bez bledow NoSuchMethodError wynikajacych z roznic API.
     */
    private void startSmoothBorderShrink(WorldBorder border, double targetSize, long seconds) {
        double safeTarget = Math.max(1.0D, targetSize);
        long safeSeconds = Math.max(1L, seconds);

        // Paper 1.21.11+: changeSize przyjmuje ticki.
        try {
            org.bukkit.WorldBorder.class
                .getMethod("changeSize", double.class, long.class)
                .invoke(border, safeTarget, safeSeconds * 20L);
            return;
        } catch (NoSuchMethodException ignored) {
            // Paper 1.21.4 i starsze: uzywamy setSize(newSize, seconds).
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Nie udalo sie uruchomic plynnego bordera przez changeSize: " + exception.getMessage());
        }

        try {
            org.bukkit.WorldBorder.class
                .getMethod("setSize", double.class, long.class)
                .invoke(border, safeTarget, safeSeconds);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Nie udalo sie uruchomic plynnego bordera; ustawiam rozmiar awaryjnie.");
            border.setSize(safeTarget);
        }
    }

    public void onDeath(Player player) {
        RandomizerQueue queue = membership.get(player.getUniqueId());
        if (queue == null || queue.state() != GameState.RUNNING) return;
        eliminated.add(player.getUniqueId());
        stats.addDeath(player);
        Player killer = player.getKiller();
        if (killer != null && membership.get(killer.getUniqueId()) == queue) {
            kills.merge(killer.getUniqueId(), 1, Integer::sum);
            stats.addKill(killer);
        }

        Map<String, String> deathPlaceholders = new HashMap<>();
        deathPlaceholders.put("player", player.getName());
        deathPlaceholders.put("killer", killer == null ? "brak" : killer.getName());
        deathPlaceholders.put("alive", String.valueOf(Math.max(0, alive(queue).size())));
        broadcast(queue, config.msgDeath, deathPlaceholders);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isDead()) player.spigot().respawn();
        });
        checkWinner(queue);
    }

    public void onRespawn(Player player) {
        RandomizerQueue queue = membership.get(player.getUniqueId());
        if (queue == null || !eliminated.contains(player.getUniqueId())) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(queue.session().center());
            player.setWorldBorder(queue.session().border());
            player.getInventory().clear();
            player.showTitle(Title.title(
                Text.mm(config.titleLoser, placeholders(queue, player)),
                Text.mm(config.subtitleLoser, placeholders(queue, player)),
                Title.Times.times(Duration.ofMillis(150), Duration.ofSeconds(4), Duration.ofMillis(500))
            ));
            player.sendActionBar(Text.mm(config.actionbarSpectator, placeholders(queue, player)));
        });
    }

    public Location respawnLocation(Player player) {
        RandomizerQueue queue = membership.get(player.getUniqueId());
        return queue == null ? null : queue.session().center();
    }

    public boolean handleLeaveItem(Player player, ItemStack item) {
        if (item == null || item.getType() != Material.RED_DYE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(leaveItemKey, PersistentDataType.BYTE)) return false;
        leave(player);
        return true;
    }

    private void checkWinner(RandomizerQueue queue) {
        if (queue.state() != GameState.RUNNING) return;
        List<Player> alive = alive(queue);
        Set<Integer> teams = new HashSet<>();
        List<UUID> order = new ArrayList<>(queue.players());
        for (Player player : alive) {
            int index = order.indexOf(player.getUniqueId());
            if (index >= 0) teams.add(index / queue.mode().teamSize());
        }
        if (teams.size() <= 1) end(queue, alive);
    }

    private void end(RandomizerQueue queue, List<Player> winners) {
        if (queue.state() == GameState.ENDING) return;
        queue.state(GameState.ENDING);
        String names = winners.stream().map(Player::getName).reduce((a, b) -> a + ", " + b).orElse(config.msgNoWinner);
        Map<String, String> ph = Map.of("winner", names);
        for (Player winner : winners) stats.addWin(winner);
        stats.save();
        for (Player player : online(queue)) {
            player.showTitle(Title.title(
                Text.mm(config.titleWinner, ph),
                Text.mm(config.subtitleWinner, ph),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(4), Duration.ofMillis(750))
            ));
            player.sendMessage(Text.mm(config.msgWinnerChat, ph));
            player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> finish(queue), 100L);
    }

    private void finish(RandomizerQueue queue) {
        for (Player player : online(queue)) {
            membership.remove(player.getUniqueId());
            eliminated.remove(player.getUniqueId());
            restorePlayer(player);
            updateLobbyVisuals(player);
        }
        removeArena(queue);
    }

    private void removeArena(RandomizerQueue queue) {
        arenas.remove(queue);
        ArenaSession session = queue.session();
        preparedArenas.stream()
            .filter(arena -> arena.slotIndex == session.slot())
            .filter(arena -> arena.center.getWorld().equals(session.center().getWorld()))
            .filter(arena -> arena.center.distanceSquared(session.center()) < 1.0D)
            .findFirst()
            .ifPresent(arena -> arena.inUse = false);
    }

    private void prepareWaitingPlayer(Player player, RandomizerQueue queue) {
        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
        player.teleport(waitingSpawn(queue.session(), queue.players().size() - 1));
        player.setRespawnLocation(queue.session().center(), true);
        player.setWorldBorder(queue.session().border());
        player.getInventory().setItem(4, leaveItem());
    }

    private void prepareFightingPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0f);
    }

    private ItemStack leaveItem() {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.mm(config.leaveItemName));
        meta.lore(config.leaveItemLore.stream().map(Text::mm).toList());
        meta.getPersistentDataContainer().set(leaveItemKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private Location waitingSpawn(ArenaSession session, int index) {
        double angle = Math.toRadians((index * 90) % 360);
        int x = (int) Math.round(session.center().getX() + Math.cos(angle) * 6.0);
        int z = (int) Math.round(session.center().getZ() + Math.sin(angle) * 6.0);
        Location safe = safeSurface(session.world(), x, z, 24);
        return safe != null ? safe : session.center().clone();
    }

    private List<Location> generatedSpawns(ArenaSession session, int count) {
        List<Location> result = new ArrayList<>();
        double radius = Math.max(35.0, Math.min(config.borderGameSize * 0.38, 120.0));
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 * i) / count;
            int x = (int) Math.round(session.center().getX() + Math.cos(angle) * radius);
            int z = (int) Math.round(session.center().getZ() + Math.sin(angle) * radius);
            Location spawn = safeSurface(session.world(), x, z, 32);
            if (spawn == null) spawn = session.center().clone();
            spawn.setYaw((float) Math.toDegrees(Math.atan2(session.center().getZ() - spawn.getZ(), session.center().getX() - spawn.getX())) - 90f);
            result.add(spawn);
        }
        return result;
    }

    private void restorePlayer(Player player) {
        PlayerSnapshot snapshot = snapshots.remove(player.getUniqueId());
        if (snapshot != null) snapshot.restore(player);
        else if (config.lobby != null) player.teleport(config.lobby);
        kills.remove(player.getUniqueId());
        Scoreboard previous = previousScoreboards.remove(player.getUniqueId());
        player.setScoreboard(previous != null ? previous : Bukkit.getScoreboardManager().getMainScoreboard());
        player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        player.clearTitle();
        player.sendActionBar(Component.empty());
        player.setRespawnLocation(null);
    }

    private RandomizerQueue findJoinable(RandomizerMode mode) {
        return arenas.stream()
            .filter(queue -> queue.mode() == mode)
            .filter(queue -> queue.state() == GameState.WAITING || queue.state() == GameState.COUNTDOWN)
            .filter(queue -> !queue.full())
            .max(Comparator.comparingInt(queue -> queue.players().size()))
            .orElse(null);
    }

    private List<Player> online(RandomizerQueue queue) {
        return queue.players().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    private List<Player> alive(RandomizerQueue queue) {
        return online(queue).stream()
            .filter(player -> !eliminated.contains(player.getUniqueId()))
            .filter(player -> player.getGameMode() != GameMode.SPECTATOR)
            .filter(player -> !player.isDead())
            .toList();
    }

    private void broadcast(RandomizerQueue queue, String message, Map<String, String> extra) {
        for (Player player : online(queue)) {
            Map<String, String> ph = placeholders(queue, player);
            ph.putAll(extra);
            player.sendMessage(Text.mm(message, ph));
        }
    }

    private void updateVisuals(RandomizerQueue queue) {
        for (Player player : online(queue)) {
            updateScoreboard(player, queue);
            updateTab(player, queue);
        }
    }

    public void scheduleLobbyVisuals(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateLobbyVisuals(player), 1L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateLobbyVisuals(player), 10L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateLobbyVisuals(player), 40L);
    }

    public void onPlayerJoin(Player player) {
        stats.playerSeen(player);
        scheduleLobbyVisuals(player);
        Bukkit.getScheduler().runTaskLater(plugin, this::refreshPlayerVisibility, 2L);
    }

    public void onPlayerQuit(Player player) {
        stats.playerSeen(player);
        stats.save();
        Bukkit.getScheduler().runTaskLater(plugin, this::refreshPlayerVisibility, 1L);
    }

    public boolean canSeeOrChat(Player viewer, Player target) {
        RandomizerQueue viewerQueue = membership.get(viewer.getUniqueId());
        RandomizerQueue targetQueue = membership.get(target.getUniqueId());
        if (viewerQueue == null || targetQueue == null) return viewerQueue == null && targetQueue == null;
        return viewerQueue == targetQueue;
    }

    public void refreshPlayerVisibility() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player viewer : players) {
            for (Player target : players) {
                if (viewer.equals(target)) continue;
                if (canSeeOrChat(viewer, target)) viewer.showPlayer(plugin, target);
                else viewer.hidePlayer(plugin, target);
            }
        }
    }

    public void updateLobbyVisuals(Player player) {
        if (player == null || !player.isOnline() || membership.containsKey(player.getUniqueId())) return;
        updateLobbyScoreboard(player);
        updateLobbyTab(player);
    }

    private void updateLobbyVisuals() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateLobbyVisuals(player);
        }
    }

    private void updateLobbyScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(
            "randomizer_lobby",
            "dummy",
            Text.legacy(config.lobbyScoreboardTitle, Map.of())
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Map<String, String> ph = lobbyPlaceholders(player);
        List<String> lines = config.lobbyScoreboardLines;
        int score = Math.min(15, lines.size());
        int unique = 0;
        for (String line : lines.subList(Math.max(0, lines.size() - 15), lines.size())) {
            setLine(objective, Text.legacy(line, ph), score--, unique++);
        }
        player.setScoreboard(scoreboard);
    }

    private void updateLobbyTab(Player player) {
        Map<String, String> ph = lobbyPlaceholders(player);
        String header = String.join("\n", config.lobbyTabHeader);
        String footer = String.join("\n", config.lobbyTabFooter);
        player.sendPlayerListHeaderAndFooter(Text.mm(header, ph), Text.mm(footer, ph));
    }

    private Map<String, String> lobbyPlaceholders(Player player) {
        Map<String, String> ph = new HashMap<>();
        long totalSeconds = stats.playTimeSeconds(player.getUniqueId());
        ph.put("player", player.getName());
        ph.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        ph.put("website", config.scoreboardWebsite);
        ph.put("discord", config.scoreboardDiscord);
        ph.put("wins", String.valueOf(stats.wins(player.getUniqueId())));
        ph.put("kills", String.valueOf(stats.kills(player.getUniqueId())));
        ph.put("deaths", String.valueOf(stats.deaths(player.getUniqueId())));
        ph.put("hours", String.valueOf(totalSeconds / 3600));
        ph.put("minutes", String.valueOf((totalSeconds % 3600) / 60));
        ph.put("seconds", String.valueOf(totalSeconds % 60));
        return ph;
    }

    private void updateScoreboard(Player player, RandomizerQueue queue) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("randomizer", "dummy", Text.legacy(config.scoreboardTitle, Map.of()));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Map<String, String> ph = placeholders(queue, player);
        int left = queue.state() == GameState.RUNNING ? Math.max(0, config.gameDurationSeconds - queue.elapsed()) : config.gameDurationSeconds;
        ph.put("minutes", String.valueOf(left / 60));
        ph.put("seconds", String.valueOf(left % 60));
        int borderSize = (int) Math.round(queue.session().border().getSize());
        ph.put("border", borderSize + "x" + borderSize);
        ph.put("border-size", String.valueOf(borderSize));
        ph.put("kills", String.valueOf(kills.getOrDefault(player.getUniqueId(), 0)));
        ph.put("players", String.valueOf(queue.state() == GameState.RUNNING ? alive(queue).size() : queue.players().size()));

        ph.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        ph.put("website", config.scoreboardWebsite);
        ph.put("discord", config.scoreboardDiscord);
        ph.put("wins", String.valueOf(stats.wins(player.getUniqueId())));
        ph.put("deaths", String.valueOf(stats.deaths(player.getUniqueId())));

        List<String> lines = config.scoreboardLines;
        int score = Math.min(15, lines.size());
        int unique = 0;
        for (String line : lines.subList(Math.max(0, lines.size() - 15), lines.size())) {
            setLine(objective, Text.legacy(line, ph), score--, unique++);
        }
        player.setScoreboard(scoreboard);
    }

    private void setLine(Objective objective, String text, int score, int unique) {
        String suffix = "§" + Integer.toHexString(unique & 15) + "§r";
        objective.getScore(text + suffix).setScore(score);
    }

    private void updateTab(Player player, RandomizerQueue queue) {
        Map<String, String> ph = placeholders(queue, player);
        ph.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        ph.put("website", config.scoreboardWebsite);
        ph.put("discord", config.scoreboardDiscord);
        String header = String.join("\n", config.tabHeader);
        String footer = String.join("\n", config.tabFooter);
        player.sendPlayerListHeaderAndFooter(Text.mm(header, ph), Text.mm(footer, ph));
    }

    private Map<String, String> placeholders(RandomizerQueue queue, Player player) {
        Map<String, String> ph = new HashMap<>();
        ph.put("mode", queue.mode().id());
        ph.put("players", String.valueOf(queue.players().size()));
        ph.put("max", String.valueOf(queue.mode().maxPlayers()));
        ph.put("player", player == null ? "" : player.getName());
        ph.put("teammate", teammateText(queue, player));
        return ph;
    }

    private String teammateText(RandomizerQueue queue, Player player) {
        if (player == null || queue.mode() == RandomizerMode.ONE_V_ONE || queue.mode() == RandomizerMode.FFA) {
            return "Solo";
        }
        List<UUID> ordered = new ArrayList<>(queue.players());
        int index = ordered.indexOf(player.getUniqueId());
        if (index < 0) return "Solo";
        int teamSize = queue.mode().teamSize();
        int teamStart = (index / teamSize) * teamSize;
        List<String> names = new ArrayList<>();
        for (int i = teamStart; i < Math.min(teamStart + teamSize, ordered.size()); i++) {
            UUID id = ordered.get(i);
            if (id.equals(player.getUniqueId())) continue;
            Player teammate = Bukkit.getPlayer(id);
            names.add(teammate == null ? "?" : teammate.getName());
        }
        return names.isEmpty() ? "Brak" : String.join(", ", names);
    }

    private String readableItem(ItemStack item) {
        String name = item.getType().name().toLowerCase().replace('_', ' ');
        String[] parts = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private String playerWord(int count) {
        return count == 1 ? "gracza" : "graczy";
    }

    public void shutdown() {
        if (ticker != null) ticker.cancel();
        if (lobbyVisualTicker != null) lobbyVisualTicker.cancel();
        if (borderPushTicker != null) borderPushTicker.cancel();
        for (UUID id : new ArrayList<>(membership.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) restorePlayer(player);
        }
        membership.clear();
        separationCooldown.clear();
        arenas.clear();
        eliminated.clear();
        kills.clear();
        snapshots.clear();
        previousScoreboards.clear();
        stats.save();
        for (PreparedArena prepared : preparedArenas) {
            for (long key : new HashSet<>(prepared.forcedChunks)) {
                int chunkX = (int) (key >> 32);
                int chunkZ = (int) key;
                if (prepared.world.isChunkLoaded(chunkX, chunkZ)) {
                    prepared.world.getChunkAt(chunkX, chunkZ).setForceLoaded(false);
                }
            }
        }
        preparedArenas.clear();
        for (WorldSlot slot : worldSlots) {
            slot.world.save();
        }
    }


    private static final class PlainsBiomeProvider extends BiomeProvider {
        @Override
        public org.bukkit.block.Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return org.bukkit.block.Biome.PLAINS;
        }

        @Override
        public List<org.bukkit.block.Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(org.bukkit.block.Biome.PLAINS);
        }
    }

    private static final class PreparedArena {
        private final int slotIndex;
        private final World world;
        private Location center;
        private final Set<Long> forcedChunks = Collections.synchronizedSet(new HashSet<>());
        private volatile boolean ready;
        private boolean inUse;

        private PreparedArena(int slotIndex, World world, Location center) {
            this.slotIndex = slotIndex;
            this.world = world;
            this.center = center;
        }
    }

    private static final class WorldSlot {
        private final int index;
        private final World world;
        private WorldSlot(int index, World world) {
            this.index = index;
            this.world = world;
        }
    }
}
