package pl.olczku.miniGames.randomizer.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Header("Randomizer - ustawienia trybu")
public class RandomizerConfig extends OkaeriConfig {
    @Comment("Czas odliczania po zapełnieniu kolejki.")
    public int countdownSeconds = 10;
    @Comment("Co ile sekund każdy żywy gracz dostaje inny losowy przedmiot.")
    public int itemIntervalSeconds = 60;
    @Comment("Po ilu sekundach border zaczyna się zmniejszać.")
    public int borderDelaySeconds = 180;
    public double borderStartSize = 120.0;
    public double borderEndSize = 8.0;
    public int borderShrinkSeconds = 180;
    public String tabHeader = "<gradient:#ff7a00:#ffcc55><bold>RANDOMIZER</bold></gradient>";
    public String tabFooter = "<gray>Tryb: <yellow>{mode}</yellow>  •  Gracze: <white>{players}/{max}</white></gray>";
    public List<String> vanillaItems = new ArrayList<>(List.of("DIAMOND_SWORD", "BOW", "CROSSBOW", "GOLDEN_APPLE", "ENDER_PEARL", "SHIELD", "TRIDENT", "COBWEB", "LAVA_BUCKET", "WATER_BUCKET", "TNT", "FLINT_AND_STEEL"));
    @Comment("ID przedmiotów Nexo. Pusta lista wyłącza losowanie Nexo.")
    public List<String> nexoItems = new ArrayList<>();
    public Location lobby;
    public Location arenaCenter;
    public List<Location> spawns1v1 = new ArrayList<>();
    public List<Location> spawns2v2 = new ArrayList<>();
    public List<Location> spawns4v4 = new ArrayList<>();
}
