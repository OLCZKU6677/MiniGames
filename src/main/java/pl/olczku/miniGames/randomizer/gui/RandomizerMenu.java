package pl.olczku.miniGames.randomizer.gui;

import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.context.RenderContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.olczku.miniGames.randomizer.game.RandomizerMode;
import pl.olczku.miniGames.randomizer.game.RandomizerService;
import pl.olczku.miniGames.randomizer.util.Text;

import java.util.List;

public final class RandomizerMenu extends View {
    private final RandomizerService service;

    public RandomizerMenu(RandomizerService service) {
        this.service = service;
    }

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {
        config.title(Text.mm("<gold><bold>RANDOMIZER</bold></gold>"));
        config.size(3);
        config.cancelOnClick();
    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {
        button(render, 11, RandomizerMode.ONE_V_ONE, Material.IRON_SWORD);
        button(render, 13, RandomizerMode.TWO_V_TWO, Material.DIAMOND_SWORD);
        button(render, 15, RandomizerMode.FOUR_V_FOUR, Material.NETHERITE_SWORD);
    }

    private void button(RenderContext render, int slot, RandomizerMode mode, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.mm("<yellow><bold>" + mode.id() + "</bold></yellow>"));
        meta.lore(List.of(
            Text.mm("<gray>Gracze: <white>" + service.queue(mode).players().size() + "/" + mode.maxPlayers() + "</white></gray>"),
            Text.mm("<green>Kliknij, aby dołączyć</green>")
        ));
        item.setItemMeta(meta);
        render.slot(slot, item).onClick(context -> {
            Player player = context.getPlayer();
            service.join(player, mode);
            player.closeInventory();
        });
    }

    public void open(Player player) {
        openForPlayer(player);
    }
}
