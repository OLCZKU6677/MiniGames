package pl.olczku.miniGames.randomizer.game;

import java.util.Locale;

public enum RandomizerMode {
    ONE_V_ONE("1v1", 2, 1, false),
    TWO_V_TWO("2v2", 4, 2, false),
    FOUR_V_FOUR("4v4", 8, 4, false),
    FFA("ffa", 30, 1, true);

    private final String id;
    private final int maxPlayers;
    private final int teamSize;
    private final boolean dynamicStart;

    RandomizerMode(String id, int maxPlayers, int teamSize, boolean dynamicStart) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.teamSize = teamSize;
        this.dynamicStart = dynamicStart;
    }

    public String id() { return id; }
    public int maxPlayers() { return maxPlayers; }
    public int teamSize() { return teamSize; }
    public boolean dynamicStart() { return dynamicStart; }
    public boolean canStart(int players) {
        return dynamicStart ? players >= 2 && players % 2 == 0 : players >= maxPlayers;
    }

    public static RandomizerMode parse(String text) {
        String value = text.toLowerCase(Locale.ROOT);
        for (RandomizerMode mode : values()) if (mode.id.equals(value)) return mode;
        throw new IllegalArgumentException("Dostepne tryby: 1v1, 2v2, 4v4, ffa");
    }
}
