package pl.olczku.miniGames.randomizer.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.olczku.miniGames.randomizer.game.RandomizerMode;
import pl.olczku.miniGames.randomizer.game.RandomizerService;
import pl.olczku.miniGames.randomizer.util.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RandomizerMenu implements Listener {
    private final RandomizerService service;

    public RandomizerMenu(RandomizerService service) {
        this.service = service;
    }

    public void open(Player player) {
        Component title = Text.mm(service.config().menuTitle);
        Inventory inventory = Bukkit.createInventory(null, 27, title);
        inventory.setItem(11, button(RandomizerMode.ONE_V_ONE, Material.IRON_SWORD));
        inventory.setItem(13, button(RandomizerMode.TWO_V_TWO, Material.DIAMOND_SWORD));
        inventory.setItem(15, button(RandomizerMode.FOUR_V_FOUR, Material.NETHERITE_SWORD));
        inventory.setItem(22, button(RandomizerMode.FFA, Material.TOTEM_OF_UNDYING));
        player.openInventory(inventory);
    }

    private ItemStack button(RandomizerMode mode, Material material) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode", mode.id());
        placeholders.put("players", String.valueOf(service.queuedPlayers(mode)));
        placeholders.put("max", String.valueOf(mode.maxPlayers()));

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.mm(service.config().menuModeName, placeholders));
        meta.lore(List.of(
            Text.mm(service.config().menuPlayers, placeholders),
            Text.mm(service.config().menuClick, placeholders)
        ));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Text.mm(service.config().menuTitle))) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        RandomizerMode mode = switch (event.getRawSlot()) {
            case 11 -> RandomizerMode.ONE_V_ONE;
            case 13 -> RandomizerMode.TWO_V_TWO;
            case 15 -> RandomizerMode.FOUR_V_FOUR;
            case 22 -> RandomizerMode.FFA;
            default -> null;
        };
        if (mode == null) return;
        player.closeInventory();
        service.join(player, mode);
    }
}
