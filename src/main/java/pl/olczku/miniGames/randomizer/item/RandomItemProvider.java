package pl.olczku.miniGames.randomizer.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.olczku.miniGames.randomizer.config.RandomizerConfig;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomItemProvider {
    private static final int RECENT_LIMIT = 10;

    private final RandomizerConfig config;
    private final List<Material> normalPool;
    private final List<Material> priorityPool;
    private final Map<UUID, Deque<Material>> recentByPlayer = new HashMap<>();

    public RandomItemProvider(RandomizerConfig config) {
        this.config = config;

        List<Material> useful = Arrays.stream(Material.values())
            .filter(Material::isItem)
            .filter(material -> !material.isAir())
            .filter(material -> !material.name().startsWith("LEGACY_"))
            .filter(this::isAllowed)
            .toList();

        this.normalPool = useful;
        this.priorityPool = useful.stream()
            .filter(this::isPriority)
            .toList();
    }

    /**
     * Losuje przedmiot dla konkretnego gracza.
     * Co trzeci drop powinien wywołać tę metodę z priorityDrop=true.
     */
    public ItemStack next(UUID playerId, boolean priorityDrop) {
        if (!config.useAllVanillaItems) {
            ItemStack configured = configuredItem();
            if (configured != null) return configured;
        }

        List<Material> source = priorityDrop && !priorityPool.isEmpty() ? priorityPool : normalPool;
        Material material = chooseWithoutRecent(playerId, source);
        remember(playerId, material);
        return new ItemStack(material, defaultAmount(material));
    }

    /** Zachowane dla zgodności ze starszym kodem. */
    public ItemStack next() {
        return next(new UUID(0L, 0L), false);
    }

    public void clearHistory(UUID playerId) {
        recentByPlayer.remove(playerId);
    }

    private ItemStack configuredItem() {
        List<ItemStack> pool = new ArrayList<>();
        for (ConfiguredItem configured : config.vanillaItems) {
            Material material = Material.matchMaterial(configured.id());
            if (material == null || material.isAir() || !material.isItem() || !isAllowed(material) || !roll(configured.chance())) continue;
            pool.add(new ItemStack(material, legalAmount(material, configured.amount())));
        }
        for (ConfiguredItem configured : config.nexoItems) {
            if (!roll(configured.chance())) continue;
            ItemStack item = nexo(configured.id());
            if (item != null && !item.getType().isAir()) {
                item.setAmount(legalAmount(item.getType(), configured.amount()));
                pool.add(item);
            }
        }
        if (pool.isEmpty()) return null;
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size())).clone();
    }

    private Material chooseWithoutRecent(UUID playerId, List<Material> source) {
        if (source.isEmpty()) return Material.COBBLESTONE;

        Deque<Material> recent = recentByPlayer.get(playerId);
        List<Material> available = source;
        if (recent != null && !recent.isEmpty()) {
            List<Material> filtered = source.stream().filter(material -> !recent.contains(material)).toList();
            if (!filtered.isEmpty()) available = filtered;
        }

        // Przy zwykłym losowaniu około 55% szans na rzecz typowo PvP/przydatny surowiec.
        if (source == normalPool && !priorityPool.isEmpty() && ThreadLocalRandom.current().nextDouble() < 0.55D) {
            List<Material> preferred = priorityPool;
            if (recent != null && !recent.isEmpty()) {
                List<Material> filtered = priorityPool.stream().filter(material -> !recent.contains(material)).toList();
                if (!filtered.isEmpty()) preferred = filtered;
            }
            available = preferred;
        }

        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    private void remember(UUID playerId, Material material) {
        Deque<Material> recent = recentByPlayer.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        recent.remove(material);
        recent.addLast(material);
        while (recent.size() > RECENT_LIMIT) recent.removeFirst();
    }

    private boolean isAllowed(Material material) {
        String name = material.name();

        if (material == Material.BARRIER || material == Material.COMMAND_BLOCK
            || material == Material.CHAIN_COMMAND_BLOCK || material == Material.REPEATING_COMMAND_BLOCK
            || material == Material.STRUCTURE_BLOCK || material == Material.STRUCTURE_VOID
            || material == Material.JIGSAW || material == Material.DEBUG_STICK) return false;

        // Dekoracje i przedmioty, z których praktycznie nie ma pożytku w walce.
        String[] blocked = {
            "_FLOWER", "TULIP", "ORCHID", "DANDELION", "POPPY", "LILY_OF_THE_VALLEY",
            "CORNFLOWER", "AZURE_BLUET", "OXEYE_DAISY", "WITHER_ROSE", "PITCHER_PLANT",
            "TORCHFLOWER", "SPORE_BLOSSOM", "HANGING_ROOTS", "SEAGRASS", "KELP", "VINE",
            "SAPLING", "AZALEA", "BUSH", "FERN", "GRASS", "DEAD_BUSH",
            "_FENCE", "FENCE_GATE", "_DOOR", "_TRAPDOOR", "_BUTTON", "PRESSURE_PLATE",
            "_SIGN", "HANGING_SIGN", "_BANNER", "_CARPET", "_BED", "_CANDLE",
            "_SLAB", "_STAIRS", "_WALL", "_PANE", "_GLASS", "GLASS_",
            "MUSIC_DISC", "POTTERY_SHERD", "BANNER_PATTERN", "SMITHING_TEMPLATE",
            "SPAWN_EGG", "MINECART", "_BOAT", "_RAFT", "PAINTING", "ITEM_FRAME",
            "ARMOR_STAND", "FLOWER_POT", "DECORATED_POT", "HEAD", "SKULL",
            "CORAL", "FAN", "GLOW_LICHEN", "SCULK_VEIN", "WEB"
        };
        for (String part : blocked) {
            if (name.contains(part)) return false;
        }

        return true;
    }

    private boolean isPriority(Material material) {
        String name = material.name();

        if (name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE")
            || name.endsWith("_SHOVEL") || name.endsWith("_HOE")) return true;

        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
            || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) return true;

        if (material == Material.BOW || material == Material.CROSSBOW || material == Material.SHIELD
            || material == Material.TRIDENT || material == Material.MACE || material == Material.ARROW
            || material == Material.SPECTRAL_ARROW || material == Material.TIPPED_ARROW
            || material == Material.FLINT_AND_STEEL || material == Material.FISHING_ROD) return true;

        return name.endsWith("_ORE") || name.contains("DEEPSLATE_") && name.endsWith("_ORE")
            || material == Material.COAL || material == Material.RAW_IRON || material == Material.RAW_GOLD
            || material == Material.RAW_COPPER || material == Material.IRON_INGOT
            || material == Material.GOLD_INGOT || material == Material.COPPER_INGOT
            || material == Material.DIAMOND || material == Material.EMERALD
            || material == Material.NETHERITE_SCRAP || material == Material.NETHERITE_INGOT
            || material == Material.LAPIS_LAZULI || material == Material.REDSTONE;
    }

    private int defaultAmount(Material material) {
        String name = material.name();
        if (name.endsWith("_ORE") || material == Material.RAW_IRON || material == Material.RAW_GOLD
            || material == Material.RAW_COPPER || material == Material.IRON_INGOT
            || material == Material.GOLD_INGOT || material == Material.COPPER_INGOT
            || material == Material.DIAMOND || material == Material.EMERALD
            || material == Material.NETHERITE_SCRAP || material == Material.LAPIS_LAZULI
            || material == Material.REDSTONE || material == Material.COAL) {
            return Math.min(8, material.getMaxStackSize());
        }
        if (material == Material.ARROW || material == Material.SPECTRAL_ARROW) return 16;
        if (material.isBlock() && material.getMaxStackSize() >= 64) return 32;
        if (material.isEdible()) return Math.min(12, material.getMaxStackSize());
        if (material == Material.ENDER_PEARL || material == Material.SNOWBALL || material == Material.EGG) {
            return Math.min(8, material.getMaxStackSize());
        }
        return material.getMaxStackSize() == 1 ? 1 : Math.min(material.getMaxStackSize(), 16);
    }

    private boolean roll(double chance) {
        return chance >= 100.0 || ThreadLocalRandom.current().nextDouble(100.0) < chance;
    }

    private int legalAmount(Material material, int requested) {
        return Math.max(1, Math.min(requested, material.getMaxStackSize()));
    }

    private ItemStack nexo(String id) {
        if (Bukkit.getPluginManager().getPlugin("Nexo") == null) return null;
        try {
            Class<?> api = Class.forName("com.nexomc.nexo.api.NexoItems");
            Method method = api.getMethod("itemFromId", String.class);
            Object builder = method.invoke(null, id);
            return builder == null ? null : (ItemStack) builder.getClass().getMethod("build").invoke(builder);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
