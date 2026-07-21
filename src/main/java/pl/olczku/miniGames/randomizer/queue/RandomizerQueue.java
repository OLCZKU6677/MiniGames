package pl.olczku.miniGames.randomizer.queue;

import pl.olczku.miniGames.randomizer.game.GameState;
import pl.olczku.miniGames.randomizer.game.RandomizerMode;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class RandomizerQueue {
    private final RandomizerMode mode;
    private final LinkedHashSet<UUID> players = new LinkedHashSet<>();
    private GameState state = GameState.WAITING;
    private int countdown;

    public RandomizerQueue(RandomizerMode mode) { this.mode = mode; }
    public RandomizerMode mode() { return mode; }
    public Set<UUID> players() { return Collections.unmodifiableSet(players); }
    public boolean add(UUID id) { return players.size() < mode.maxPlayers() && players.add(id); }
    public boolean remove(UUID id) { return players.remove(id); }
    public boolean full() { return players.size() >= mode.maxPlayers(); }
    public GameState state() { return state; }
    public void state(GameState state) { this.state = state; }
    public int countdown() { return countdown; }
    public void countdown(int countdown) { this.countdown = countdown; }
    public void clear() { players.clear(); state = GameState.WAITING; countdown = 0; }
}
