package pl.olczku.miniGames.randomizer.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public record ArenaSession(int slot, World world, Location center, WorldBorder border) {
}
