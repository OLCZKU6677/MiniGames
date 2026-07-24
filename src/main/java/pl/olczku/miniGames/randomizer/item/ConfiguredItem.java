package pl.olczku.miniGames.randomizer.item;

public record ConfiguredItem(String id, int amount, double chance) {
    public ConfiguredItem {
        amount = Math.max(1, Math.min(64, amount));
        chance = Math.max(0.0, Math.min(100.0, chance));
    }
}
