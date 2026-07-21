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

import java.util.List;

public final class RandomizerMenu implements Listener {
    private static final Component TITLE = Text.mm("<gold><bold>RANDOMIZER</bold></gold>");
    private final RandomizerService service;

    public RandomizerMenu(RandomizerService service) {
        this.service = service;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        inventory.setItem(11, button(RandomizerMode.ONE_V_ONE, Material.IRON_SWORD));
        inventory.setItem(13, button(RandomizerMode.TWO_V_TWO, Material.DIAMOND_SWORD));
        inventory.setItem(15, button(RandomizerMode.FOUR_V_FOUR, Material.NETHERITE_SWORD));
        player.openInventory(inventory);
    }

    private ItemStack button(RandomizerMode mode, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.mm("<yellow><bold>" + mode.id() + "</bold></yellow>"));
        meta.lore(List.of(
            Text.mm("<gray>Gracze: <white>" + service.queue(mode).players().size() + "/" + mode.maxPlayers() + "</white></gray>"),
            Text.mm("<green>Kliknij, aby dołączyć</green>")
        ));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(TITLE)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        RandomizerMode mode = switch (event.getRawSlot()) {
            case 11 -> RandomizerMode.ONE_V_ONE;
            case 13 -> RandomizerMode.TWO_V_TWO;
            case 15 -> RandomizerMode.FOUR_V_FOUR;
            default -> null;
        };
        if (mode == null) return;
        player.closeInventory();
        service.join(player, mode);
    }
}
