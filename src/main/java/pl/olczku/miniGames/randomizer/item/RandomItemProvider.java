package pl.olczku.miniGames.randomizer.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.olczku.miniGames.randomizer.config.RandomizerConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomItemProvider {
    private final RandomizerConfig config;

    public RandomItemProvider(RandomizerConfig config) {
        this.config = config;
    }

    public ItemStack next() {
        List<ItemStack> pool = new ArrayList<>();
        for (String name : config.vanillaItems) {
            Material material = Material.matchMaterial(name);
            if (material != null && !material.isAir()) pool.add(new ItemStack(material));
        }
        for (String id : config.nexoItems) {
            ItemStack item = nexo(id);
            if (item != null) pool.add(item);
        }
        if (pool.isEmpty()) return new ItemStack(Material.STONE);
        ItemStack result = pool.get(ThreadLocalRandom.current().nextInt(pool.size())).clone();
        result.setAmount(1);
        return result;
    }

    private ItemStack nexo(String id) {
        if (Bukkit.getPluginManager().getPlugin("Nexo") == null) return null;
        try {
            Class<?> api = Class.forName("com.nexomc.nexo.api.NexoItems");
            Method itemFromId = api.getMethod("itemFromId", String.class);
            Object builder = itemFromId.invoke(null, id);
            if (builder == null) return null;
            return (ItemStack) builder.getClass().getMethod("build").invoke(builder);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
