package pl.olczku.miniGames.randomizer.queue;

import pl.olczku.miniGames.randomizer.game.ArenaSession;
import pl.olczku.miniGames.randomizer.game.GameState;
import pl.olczku.miniGames.randomizer.game.RandomizerMode;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class RandomizerQueue {
    private final UUID id = UUID.randomUUID();
    private final RandomizerMode mode;
    private final ArenaSession session;
    private final LinkedHashSet<UUID> players = new LinkedHashSet<>();
    private GameState state = GameState.WAITING;
    private int countdown;
    private int elapsed;
    private boolean borderShrinkStarted;

    public RandomizerQueue(RandomizerMode mode, ArenaSession session) {
        this.mode = mode;
        this.session = session;
    }

    public UUID id() { return id; }
    public RandomizerMode mode() { return mode; }
    public ArenaSession session() { return session; }
    public Set<UUID> players() { return Collections.unmodifiableSet(players); }
    public boolean add(UUID id) { return players.size() < mode.maxPlayers() && players.add(id); }
    public boolean remove(UUID id) { return players.remove(id); }
    public boolean full() { return players.size() >= mode.maxPlayers(); }
    public boolean canFit(int amount) {
        return amount > 0 && players.size() + amount <= mode.maxPlayers();
    }

    public boolean readyToStart() {
        return mode.canStart(players.size());
    }
    public GameState state() { return state; }
    public void state(GameState state) { this.state = state; }
    public int countdown() { return countdown; }
    public void countdown(int countdown) { this.countdown = countdown; }
    public int elapsed() { return elapsed; }
    public int tickElapsed() { return ++elapsed; }
    public void resetElapsed() {
        elapsed = 0;
        borderShrinkStarted = false;
    }
    public boolean borderShrinkStarted() { return borderShrinkStarted; }
    public void borderShrinkStarted(boolean borderShrinkStarted) { this.borderShrinkStarted = borderShrinkStarted; }
}
