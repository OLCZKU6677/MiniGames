package pl.olczku.miniGames.randomizer.game;

import java.util.Locale;

public enum RandomizerMode {
    ONE_V_ONE("1v1", 2, 1), TWO_V_TWO("2v2", 4, 2), FOUR_V_FOUR("4v4", 8, 4);
    private final String id;
    private final int maxPlayers;
    private final int teamSize;

    RandomizerMode(String id, int maxPlayers, int teamSize) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.teamSize = teamSize;
    }

    public String id() { return id; }
    public int maxPlayers() { return maxPlayers; }
    public int teamSize() { return teamSize; }

    public static RandomizerMode parse(String text) {
        String value = text.toLowerCase(Locale.ROOT);
        for (RandomizerMode mode : values()) {
            if (mode.id.equals(value)) return mode;
        }
        throw new IllegalArgumentException("Dostępne tryby: 1v1, 2v2, 4v4");
    }
}
