package pl.olczku.miniGames.randomizer.game;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record PlayerSnapshot(Location location, GameMode gameMode, ItemStack[] inventory,
                             ItemStack[] armor, int level, float exp, double health, int food) {
    public static PlayerSnapshot take(Player player) {
        return new PlayerSnapshot(player.getLocation(), player.getGameMode(),
            player.getInventory().getContents(), player.getInventory().getArmorContents(),
            player.getLevel(), player.getExp(), player.getHealth(), player.getFoodLevel());
    }

    public void restore(Player player) {
        player.getInventory().setContents(inventory);
        player.getInventory().setArmorContents(armor);
        player.setGameMode(gameMode);
        player.setLevel(level);
        player.setExp(exp);
        player.setFoodLevel(food);
        player.setHealth(Math.min(health, player.getMaxHealth()));
        player.teleport(location);
        player.setWorldBorder(null);
    }
}
