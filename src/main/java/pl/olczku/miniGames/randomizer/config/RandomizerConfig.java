package pl.olczku.miniGames.randomizer.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pl.olczku.miniGames.randomizer.item.ConfiguredItem;

public final class RandomizerConfig {
    public int countdownSeconds;
    public int itemIntervalSeconds;
    public int gameDurationSeconds;
    public int borderDelaySeconds;
    public double borderWaitingSize;
    public double borderFfaWaitingSize;
    public double borderGameSize;
    public double borderEndSize;
    public int borderShrinkSeconds;
    public int worldCount;
    public int arenaMinDistance;
    public int arenaMaxDistance;
    public boolean preloadArenaChunks;
    public boolean keepArenaChunksLoaded;
    public int preloadArenaCount;
    public int preloadChunksPerTick;
    public String scoreboardWebsite;
    public String scoreboardDiscord;
    public List<ConfiguredItem> vanillaItems;
    public boolean useAllVanillaItems;
    public List<ConfiguredItem> nexoItems;
    public Location lobby;
    public boolean terrainEnabled;
    public int terrainSize;
    public int terrainDepth;
    public int terrainClearHeight;
    public int terrainMaxTrees;

    public String msgAlreadyPlaying;
    public String msgGameRunning;
    public String msgQueueFull;
    public String msgNoArena;
    public String msgJoin;
    public String msgLeave;
    public String msgNotPlaying;
    public String msgGameStarted;
    public String msgGoodLuck;
    public String msgBorderShrink;
    public String msgDeath;
    public String msgWinnerChat;
    public String msgOnlyPlayers;
    public String msgUsageJoin;
    public String msgUsageMain;
    public String msgInvalidMode;
    public String msgNoWinner;
    public String titleWaiting;
    public String subtitleWaiting;
    public String titleCountdown;
    public String subtitleCountdown;
    public String titleStart;
    public String subtitleStart;
    public String titleWinner;
    public String subtitleWinner;
    public String titleLoser;
    public String subtitleLoser;
    public String actionbarItemCountdown;
    public String actionbarItemReceived;
    public String actionbarSpectator;
    public String leaveItemName;
    public List<String> leaveItemLore;
    public String scoreboardTitle;
    public List<String> scoreboardLines;
    public List<String> tabHeader;
    public List<String> tabFooter;
    public String lobbyScoreboardTitle;
    public List<String> lobbyScoreboardLines;
    public List<String> lobbyTabHeader;
    public List<String> lobbyTabFooter;
    public String menuTitle;
    public String menuModeName;
    public String menuPlayers;
    public String menuClick;
    public String adminNoPermission;
    public String adminUsage;
    public String adminArenaNotFound;
    public String adminPlayerNotFound;
    public String adminTeleported;
    public String adminStoppedMessage;
    public String adminListHeader;
    public String adminListLine;
    public String adminNoActiveArenas;
    public String adminStopTitle;
    public String adminStopSubtitle;

    public static RandomizerConfig load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "randomizer.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        int configVersion = yaml.getInt("config-version", 0);
        if (configVersion < 3) {
            yaml.set("config-version", 3);
            yaml.set("item-interval-seconds", 15);
            yaml.set("border.waiting-size", 25.0);
            yaml.set("border.game-size", 300.0);
            yaml.set("worlds.count", 4);
            yaml.set("terrain.enabled", true);
            yaml.set("messages.already-playing", "&cJesteś już w kolejce lub grze.");
            yaml.set("messages.game-running", "");
            yaml.set("messages.queue-full", "&cTa arena jest pełna.");
            yaml.set("messages.no-arena", "&cWszystkie areny są obecnie zajęte. Spróbuj ponownie za chwilę.");
            yaml.set("messages.join", "&a✔ Przenoszę Cię na arenę Randomizer.");
            yaml.set("messages.leave", "&eOpuszczono Randomizer.");
            yaml.set("messages.not-playing", "&cNie jesteś w Randomizerze.");
            yaml.set("messages.game-started", "&6&lRANDOMIZER &8» &eGra wystartowała!");
            yaml.set("messages.good-luck", "&aPowodzenia!");
            yaml.set("messages.border-shrink", "");
            yaml.set("messages.death", "&7☠ &f{player} &7zginął!");
            yaml.set("messages.winner-chat", "&6🏆 Gracz &f{winner} &6wygrał grę!");
            yaml.set("messages.only-players", "&cTa komenda jest tylko dla graczy.");
            yaml.set("messages.usage-join", "&eUżycie: &f/randomizer dolacz <1v1|2v2|4v4>");
            yaml.set("messages.usage-main", "&eUżycie: &f/randomizer [dolacz <1v1|2v2|4v4>|opusc]");
            yaml.set("messages.invalid-mode", "&cNieprawidłowy tryb. Dostępne: &f1v1, 2v2, 4v4&c.");
            yaml.set("titles.waiting.title", "&4&lᴏᴄᴢᴇᴋɪᴡᴀɴɪᴇ...");
            yaml.set("titles.waiting.subtitle", "&4🔥 &cɢʀᴀᴄᴢʏ &4{players}/{max} &8✦ &cʙʀᴀᴋᴜᴊᴇ &4{missing} {player-word} &c🔥");
            yaml.set("titles.countdown.title", "&6&lSTART ZA {seconds}");
            yaml.set("titles.countdown.subtitle", "&7Przygotuj się do walki!");
            yaml.set("titles.start.title", "&6&lRANDOMIZER");
            yaml.set("titles.start.subtitle", "&aPowodzenia!");
            yaml.set("titles.winner.title", "&6&l🏆 {winner}");
            yaml.set("titles.winner.subtitle", "&eWygrał grę!");
            yaml.set("titles.loser.title", "&c&lPRZEGRAŁEŚ!");
            yaml.set("titles.loser.subtitle", "&7Możesz obserwować grę lub wpisać &c/opusc");
            yaml.set("actionbar.item-countdown", "&e🌞 Losowanie przedmiotu za &f{seconds}s &e🌞");
            yaml.set("actionbar.item-received", "&6✹ &aWylosowano przedmiot: &f{item} &6✹");
            yaml.set("actionbar.spectator", "&e✦ Aby powrócić na spawna wpisz &c/opusc &e✦");
            yaml.set("leave-item.name", "&c&l✖ Opuść arenę");
            yaml.set("leave-item.lore", List.of("&7Kliknij PPM, aby opuścić", "&7rozgrywkę Randomizer."));
            yaml.set("tab.header", "&6&lRANDOMIZER");
            yaml.set("tab.footer", "&7Tryb: &e{mode} &8• &7Gracze: &f{players}/{max}");
            yaml.set("menu.title", "&6&lRANDOMIZER");
            yaml.set("menu.mode-name", "&e&l{mode}");
            yaml.set("menu.players", "&7Gracze: &f{players}/{max}");
            yaml.set("menu.click", "&aKliknij, aby dołączyć");
        }

        if (configVersion < 4) {
            yaml.set("config-version", 4);
            yaml.set("scoreboard.time", null);
            yaml.set("scoreboard.players", null);
            yaml.set("scoreboard.border", null);
            yaml.set("scoreboard.kills", null);
            yaml.set("scoreboard.website", "www.zagrajtu.pl");
            yaml.set("scoreboard.discord", "ᴡᴡᴡ.ᴅᴄ.ᴢᴀɢʀᴀᴊᴛᴜ.ᴘʟ");
            if (!yaml.isList("scoreboard.lines")) {
                yaml.set("scoreboard.lines", List.of(
                    "",
                    "&7Nick: &c{player}",
                    "",
                    "&6🏆 &7Wygrane: &c{wins}",
                    "&4⚔ &7Zabójstwa: &c{kills}",
                    "&c☠ &7Śmierci: &c{deaths}",
                    "&6⌛ &7Czas: &c{minutes}min, {seconds}s",
                    "",
                    "&7Graczy online: &c{online}",
                    "",
                    "&c{website}"
                ));
            }
            yaml.set("tab.header", List.of(
                "",
                "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ",
                "&7Aktualnie gra: &c{online} graczy",
                ""
            ));
            yaml.set("tab.footer", List.of(
                "",
                "&c✦ &7Jesteś na trybie: &4&lRANDOMIZER &c✦",
                "&7Strona: &c{website} &8• &7Discord: &c{discord}",
                ""
            ));
        }

        if (configVersion < 5) {
            yaml.set("config-version", 5);
            if (!yaml.contains("spawn-scoreboard.title")) {
                yaml.set("spawn-scoreboard.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
            }
            if (!yaml.isList("spawn-scoreboard.lines")) {
                yaml.set("spawn-scoreboard.lines", List.of(
                    "",
                    "&7Nick: &c{player}",
                    "",
                    "&6🏆 &7Wygrane: &c{wins}",
                    "&4⚔ &7Zabójstwa: &c{kills}",
                    "&c☠ &7Śmierci: &c{deaths}",
                    "&6⌛ &7Czas: &c{hours}h, {minutes}min",
                    "",
                    "&7Graczy online: &c{online}",
                    "",
                    "&c{website}"
                ));
            }
            if (!yaml.isList("spawn-tab.header")) {
                yaml.set("spawn-tab.header", List.of(
                    "",
                    "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ",
                    "&7Aktualnie gra: &c{online} graczy",
                    ""
                ));
            }
            if (!yaml.isList("spawn-tab.footer")) {
                yaml.set("spawn-tab.footer", List.of(
                    "",
                    "&c✦ &7Jesteś na trybie: &4&lRANDOMIZER &c✦",
                    "&7Strona: &c{website} &8• &7Discord: &c{discord}",
                    ""
                ));
            }
        }

        if (configVersion < 6) {
            yaml.set("config-version", 6);
            yaml.set("items.use-all-vanilla", true);
            yaml.set("border.delay-seconds", 30);
            yaml.set("border.shrink-seconds", 360);
            yaml.set("messages.already-playing", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴊᴜᴢ ᴊᴇꜱᴛᴇꜱ ᴡ ᴋᴏʟᴇᴊᴄᴇ ʟᴜʙ ɢʀᴢᴇ.");
            yaml.set("messages.queue-full", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴀʀᴇɴᴀ ᴊᴇꜱᴛ ᴘᴇʟɴᴀ.");
            yaml.set("messages.no-arena", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cʙʀᴀᴋ ᴡᴏʟɴᴇᴊ ᴀʀᴇɴʏ.");
            yaml.set("messages.join", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴘʀᴢᴇɴᴏꜱᴢᴇ ɴᴀ ᴀʀᴇɴᴇ.");
            yaml.set("messages.leave", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴏᴘᴜꜱᴄɪʟᴇꜱ ᴀʀᴇɴᴇ.");
            yaml.set("messages.not-playing", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cɴɪᴇ ɢʀᴀꜱᴢ ᴡ ʀᴀɴᴅᴏᴍɪᴢᴇʀᴢᴇ.");
            yaml.set("messages.game-started", "");
            yaml.set("messages.good-luck", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴘᴏᴡᴏᴅᴢᴇɴɪᴀ!");
            yaml.set("messages.death", "&4☠ &c{player} &4ᴢɢɪɴᴀʟ!");
            yaml.set("messages.winner-chat", "&4🏆 &c{winner} &4ᴡʏɢʀᴀʟ ɢʀᴇ!");
            yaml.set("messages.usage-join", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &c/randomizer dolacz <1v1|2v2|4v4|ffa>");
            yaml.set("messages.usage-main", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &c/randomizer [dolacz <1v1|2v2|4v4|ffa>|opusc]");
            yaml.set("messages.invalid-mode", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴛʀʏʙʏ: 1v1, 2v2, 4v4, ffa.");
            yaml.set("titles.countdown.title", "&4&lꜱᴛᴀʀᴛ ᴢᴀ &c{seconds}");
            yaml.set("titles.countdown.subtitle", "&cᴘʀᴢʏɢᴏᴛᴜᴊ ꜱɪᴇ ᴅᴏ ᴡᴀʟᴋɪ!");
            yaml.set("titles.start.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
            yaml.set("titles.start.subtitle", "&cᴘᴏᴡᴏᴅᴢᴇɴɪᴀ!");
            yaml.set("actionbar.item-countdown", "&4🌞 &cʟᴏꜱᴏᴡᴀɴɪᴇ ᴘʀᴢᴇᴅᴍɪᴏᴛᴜ ᴢᴀ &4{seconds}ꜱ &c🌞");
            yaml.set("actionbar.item-received", "&4✹ &cᴡʏʟᴏꜱᴏᴡᴀɴᴏ: &4{item} &c✹");
            yaml.set("menu.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
            yaml.set("menu.mode-name", "&c&l{mode}");
            yaml.set("menu.players", "&7ɢʀᴀᴄᴢᴇ: &c{players}/{max}");
            yaml.set("menu.click", "&cᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴅᴏʟᴀᴄᴢʏᴄ");
        }

        if (configVersion < 7) {
            yaml.set("config-version", 7);
            // Jeden wspolny swiat obsluguje wiele oddalonych aren.
            yaml.set("worlds.count", 1);
        }

        if (configVersion < 8) {
            yaml.set("config-version", 8);
            yaml.set("item-interval-seconds", 20);

            List<String> gameLines = new ArrayList<>(yaml.getStringList("scoreboard.lines"));
            if (gameLines.stream().noneMatch(line -> line.contains("{border}"))) {
                int onlineIndex = -1;
                for (int i = 0; i < gameLines.size(); i++) {
                    if (gameLines.get(i).contains("{online}")) {
                        onlineIndex = i;
                        break;
                    }
                }
                String borderLine = "&7Border: &c{border}";
                if (onlineIndex >= 0) gameLines.add(onlineIndex, borderLine);
                else gameLines.add(borderLine);
                yaml.set("scoreboard.lines", gameLines);
            }
        }


        if (configVersion < 9) {
            yaml.set("config-version", 9);
            yaml.set("border.delay-seconds", 0);
            yaml.set("border.shrink-seconds", 600);
        }

        if (configVersion < 10) {
            yaml.set("config-version", 10);
            List<String> lines = new ArrayList<>(yaml.getStringList("scoreboard.lines"));
            if (lines.stream().noneMatch(line -> line.contains("{teammate}"))) {
                int nickIndex = -1;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains("{player}")) { nickIndex = i; break; }
                }
                lines.add(nickIndex >= 0 ? nickIndex + 1 : 0, "&7Teammate: &c{teammate}");
                yaml.set("scoreboard.lines", lines);
            }
        }

        if (configVersion < 11) {
            yaml.set("config-version", 11);
            yaml.set("worlds.preload-arenas", true);
            yaml.set("worlds.keep-chunks-loaded", true);
            yaml.set("worlds.preload-arena-count", 1);
            yaml.set("worlds.preload-chunks-per-tick", 4);
        }


        if (configVersion < 12) {
            yaml.set("config-version", 12);
            yaml.set("messages.no-winner", "&cɴɪᴋᴛ");
            yaml.set("messages.already-playing", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴊᴜᴢ ᴊᴇꜱᴛᴇꜱ ᴡ ᴋᴏʟᴇᴊᴄᴇ ʟᴜʙ ɢʀᴢᴇ.");
            yaml.set("messages.game-running", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cɢʀᴀ ᴊᴜᴢ ᴛʀᴡᴀ.");
            yaml.set("messages.queue-full", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴀʀᴇɴᴀ ᴊᴇꜱᴛ ᴘᴇʟɴᴀ.");
            yaml.set("messages.no-arena", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cʙʀᴀᴋ ᴡᴏʟɴᴇᴊ ᴀʀᴇɴʏ.");
            yaml.set("messages.join", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴘʀᴢᴇɴᴏꜱᴢᴇ ɴᴀ ᴀʀᴇɴᴇ.");
            yaml.set("messages.leave", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴏᴘᴜꜱᴄɪʟᴇꜱ ᴀʀᴇɴᴇ.");
            yaml.set("messages.not-playing", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cɴɪᴇ ɢʀᴀꜱᴢ ᴡ ʀᴀɴᴅᴏᴍɪᴢᴇʀᴢᴇ.");
            yaml.set("messages.game-started", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cɢʀᴀ ᴡʏꜱᴛᴀʀᴛᴏᴡᴀʟᴀ!");
            yaml.set("messages.good-luck", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴘᴏᴡᴏᴅᴢᴇɴɪᴀ!");
            yaml.set("messages.border-shrink", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cʙᴏʀᴅᴇʀ ᴢᴀᴄᴢʏɴᴀ ꜱɪᴇ ᴢᴍɴɪᴇᴊꜱᴢᴀᴄ.");
            yaml.set("messages.death", "&4☠ &c{player} &4ᴢɢɪɴᴀʟ!");
            yaml.set("messages.winner-chat", "&4🏆 &c{winner} &4ᴡʏɢʀᴀʟ ɢʀᴇ!");
            yaml.set("messages.only-players", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴛᴀ ᴋᴏᴍᴇɴᴅᴀ ᴊᴇꜱᴛ ᴛʏʟᴋᴏ ᴅʟᴀ ɢʀᴀᴄᴢʏ.");
            yaml.set("messages.usage-join", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &c/randomizer dolacz <1v1|2v2|4v4|ffa>");
            yaml.set("messages.usage-main", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &c/randomizer [dolacz <1v1|2v2|4v4|ffa>|opusc]");
            yaml.set("messages.invalid-mode", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴛʀʏʙʏ: 1v1, 2v2, 4v4, ffa.");
            yaml.set("titles.waiting.title", "&4&lᴏᴄᴢᴇᴋɪᴡᴀɴɪᴇ...");
            yaml.set("titles.waiting.subtitle", "&4🔥 &cɢʀᴀᴄᴢʏ &4{players}/{max} &8✦ &cʙʀᴀᴋᴜᴊᴇ &4{missing} {player-word} &c🔥");
            yaml.set("titles.countdown.title", "&4&lꜱᴛᴀʀᴛ ᴢᴀ &c{seconds}");
            yaml.set("titles.countdown.subtitle", "&cᴘʀᴢʏɢᴏᴛᴜᴊ ꜱɪᴇ ᴅᴏ ᴡᴀʟᴋɪ!");
            yaml.set("titles.start.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
            yaml.set("titles.start.subtitle", "&cᴘᴏᴡᴏᴅᴢᴇɴɪᴀ!");
            yaml.set("titles.winner.title", "&4&l🏆 &c{winner}");
            yaml.set("titles.winner.subtitle", "&cᴡʏɢʀᴀʟ ɢʀᴇ!");
            yaml.set("titles.loser.title", "&4&lᴘʀᴢᴇɢʀᴀʟᴇꜱ!");
            yaml.set("titles.loser.subtitle", "&cᴍᴏᴢᴇꜱᴢ ᴏʙꜱᴇʀᴡᴏᴡᴀᴄ ɢʀᴇ ʟᴜʙ ᴡᴘɪꜱᴀᴄ &4/ᴏᴘᴜꜱᴄ");
            yaml.set("actionbar.item-countdown", "&4🌞 &cʟᴏꜱᴏᴡᴀɴɪᴇ ᴘʀᴢᴇᴅᴍɪᴏᴛᴜ ᴢᴀ &4{seconds}ꜱ &c🌞");
            yaml.set("actionbar.item-received", "&4✹ &cᴡʏʟᴏꜱᴏᴡᴀɴᴏ: &4{item} &c✹");
            yaml.set("actionbar.spectator", "&4✦ &cᴀʙʏ ᴘᴏᴡʀᴏᴄɪᴄ ɴᴀ ꜱᴘᴀᴡɴ ᴡᴘɪꜱᴢ &4/ᴏᴘᴜꜱᴄ &c✦");
            yaml.set("leave-item.name", "&4&lᴏᴘᴜꜱᴄ");
            yaml.set("leave-item.lore", List.of("&cᴋʟɪᴋɴɪᴊ ᴘᴘᴍ, ᴀʙʏ ᴏᴘᴜꜱᴄɪᴄ", "&cʀᴏᴢɢʀʏᴡᴋᴇ ʀᴀɴᴅᴏᴍɪᴢᴇʀ."));
            yaml.set("scoreboard.lines", List.of("", "&cNick: &4{player}", "&cTeammate: &4{teammate}", "", "&4⚔ &cZabójstwa: &4{kills}", "&4☠ &cŚmierci: &4{deaths}", "&4⌛ &cCzas: &4{minutes}min, {seconds}s", "&cBorder: &4{border}", "", "&cGraczy online: &4{online}", "", "&c{website}"));
            yaml.set("spawn-scoreboard.lines", List.of("", "&cNick: &4{player}", "", "&4🏆 &cWygrane: &4{wins}", "&4⚔ &cZabójstwa: &4{kills}", "&4☠ &cŚmierci: &4{deaths}", "&4⌛ &cCzas: &4{hours}h, {minutes}min", "", "&cGraczy online: &4{online}", "", "&c{website}"));
            yaml.set("tab.header", List.of("", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ", "&cAktualnie gra: &4{online} &cgraczy", ""));
            yaml.set("tab.footer", List.of("", "&4✦ &cJesteś na trybie: &4&lRANDOMIZER &c✦", "&cStrona: &4{website} &8• &cDiscord: &4{discord}", ""));
            yaml.set("spawn-tab.header", yaml.get("tab.header"));
            yaml.set("spawn-tab.footer", yaml.get("tab.footer"));
            yaml.set("menu.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
            yaml.set("menu.mode-name", "&4&l{mode}");
            yaml.set("menu.players", "&cɢʀᴀᴄᴢᴇ: &4{players}/{max}");
            yaml.set("menu.click", "&cᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴅᴏʟᴀᴄᴢʏᴄ");
        }
        if (configVersion < 14) {
            yaml.set("config-version", 14);
            yaml.set("admin.messages.no-permission", "&4&lADMIN &8» &cNie masz uprawnień.");
            yaml.set("admin.messages.usage", "&4&lADMIN &8» &c/admin <list|tp <id>|playertp <nick>|stop <id>>");
            yaml.set("admin.messages.arena-not-found", "&4&lADMIN &8» &cNie znaleziono areny.");
            yaml.set("admin.messages.player-not-found", "&4&lADMIN &8» &cGracz nie jest online albo nie znajduje się na arenie.");
            yaml.set("admin.messages.teleported", "&4&lADMIN &8» &cPrzeniesiono na arenę jako spectator.");
            yaml.set("admin.messages.stopped", "&4&lADMIN &8» &cAdministrator zatrzymał arenę.");
            yaml.set("admin.messages.list-header", "&4&lADMIN &8» &cAktywne areny:");
            yaml.set("admin.messages.list-line", "&4#{id} &c{mode} &8• &c{players}/{max} &8• &c{state}");
            yaml.set("admin.messages.no-active-arenas", "&4&lADMIN &8» &cBrak aktywnych aren.");
            yaml.set("admin.titles.stopped.title", "&4&lROZGRYWKA ZATRZYMANA");
            yaml.set("admin.titles.stopped.subtitle", "&cAdministrator zatrzymał Twoją arenę.");
        }

        yaml.addDefault("config-version", 14);
        yaml.addDefault("countdown-seconds", 10);
        yaml.addDefault("item-interval-seconds", 20);
        yaml.addDefault("game-duration-seconds", 600);
        yaml.addDefault("worlds.count", 1);
        yaml.addDefault("worlds.arena-min-distance", 1500);
        yaml.addDefault("worlds.arena-max-distance", 12000);
        yaml.addDefault("worlds.preload-arenas", true);
        yaml.addDefault("worlds.keep-chunks-loaded", true);
        yaml.addDefault("worlds.preload-arena-count", 1);
        yaml.addDefault("worlds.preload-chunks-per-tick", 4);
        yaml.addDefault("border.delay-seconds", 0);
        yaml.addDefault("border.waiting-size", 25.0);
        yaml.addDefault("border.ffa-waiting-size", 35.0);
        yaml.addDefault("border.game-size", 300.0);
        yaml.addDefault("border.end-size", 5.0);
        yaml.addDefault("border.shrink-seconds", 600);
        yaml.addDefault("scoreboard.website", "www.zagrajtu.pl");
        yaml.addDefault("scoreboard.discord", "ᴡᴡᴡ.ᴅᴄ.ᴢᴀɢʀᴀᴊᴛᴜ.ᴘʟ");
        yaml.addDefault("items.use-all-vanilla", true);
        // Nie nadpisujemy istniejącej sekcji items. Stary format listy nadal jest obsługiwany.
        if (!yaml.contains("items.vanilla")) {
            yaml.set("items.vanilla.DIAMOND_SWORD.amount", 1);
            yaml.set("items.vanilla.DIAMOND_SWORD.chance", 100.0);
            yaml.set("items.vanilla.OAK_PLANKS.amount", 32);
            yaml.set("items.vanilla.OAK_PLANKS.chance", 100.0);
            yaml.set("items.vanilla.COBBLESTONE.amount", 32);
            yaml.set("items.vanilla.COBBLESTONE.chance", 100.0);
            yaml.set("items.vanilla.GOLDEN_APPLE.amount", 2);
            yaml.set("items.vanilla.GOLDEN_APPLE.chance", 35.0);
        }
        if (!yaml.contains("items.nexo")) yaml.createSection("items.nexo");
        yaml.addDefault("lobby", null);
        yaml.addDefault("terrain.enabled", true);
        yaml.addDefault("terrain.size", 300);
        yaml.addDefault("terrain.depth", 5);
        yaml.addDefault("terrain.clear-height", 12);
        yaml.addDefault("terrain.max-trees", 4);

        yaml.addDefault("messages.already-playing", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴊᴜᴢ ᴊᴇꜱᴛᴇꜱ ᴡ ᴋᴏʟᴇᴊᴄᴇ ʟᴜʙ ɢʀᴢᴇ.");
        yaml.addDefault("messages.game-running", "");
        yaml.addDefault("messages.queue-full", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴀʀᴇɴᴀ ᴊᴇꜱᴛ ᴘᴇʟɴᴀ.");
        yaml.addDefault("messages.no-arena", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cʙʀᴀᴋ ᴡᴏʟɴᴇᴊ ᴀʀᴇɴʏ.");
        yaml.addDefault("messages.join", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴘʀᴢᴇɴᴏꜱᴢᴇ ɴᴀ ᴀʀᴇɴᴇ.");
        yaml.addDefault("messages.leave", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴏᴘᴜꜱᴄɪʟᴇꜱ ᴀʀᴇɴᴇ.");
        yaml.addDefault("messages.not-playing", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cɴɪᴇ ɢʀᴀꜱᴢ ᴡ ʀᴀɴᴅᴏᴍɪᴢᴇʀᴢᴇ.");
        yaml.addDefault("messages.game-started", "");
        yaml.addDefault("messages.good-luck", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴘᴏᴡᴏᴅᴢᴇɴɪᴀ!");
        yaml.addDefault("messages.border-shrink", "");
        yaml.addDefault("messages.death", "&4☠ &c{player} &4ᴢɢɪɴᴀʟ!");
        yaml.addDefault("messages.winner-chat", "&4🏆 &c{winner} &4ᴡʏɢʀᴀʟ ɢʀᴇ!");
        yaml.addDefault("messages.only-players", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴛᴀ ᴋᴏᴍᴇɴᴅᴀ ᴊᴇꜱᴛ ᴛʏʟᴋᴏ ᴅʟᴀ ɢʀᴀᴄᴢʏ.");
        yaml.addDefault("messages.usage-join", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &c/randomizer dolacz <1v1|2v2|4v4|ffa>");
        yaml.addDefault("messages.usage-main", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &c/randomizer [dolacz <1v1|2v2|4v4|ffa>|opusc]");
        yaml.addDefault("messages.invalid-mode", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ &8» &cᴛʀʏʙʏ: 1v1, 2v2, 4v4, ffa.");
        yaml.addDefault("messages.no-winner", "&cNikt");

        yaml.addDefault("titles.waiting.title", "&4&lᴏᴄᴢᴇᴋɪᴡᴀɴɪᴇ...");
        yaml.addDefault("titles.waiting.subtitle", "&4🔥 &cɢʀᴀᴄᴢʏ &4{players}/{max} &c✦ ʙʀᴀᴋᴜᴊᴇ &4{missing} {player-word} &c🔥");
        yaml.addDefault("titles.countdown.title", "&4&lꜱᴛᴀʀᴛ ᴢᴀ &c{seconds}");
        yaml.addDefault("titles.countdown.subtitle", "&cᴘʀᴢʏɢᴏᴛᴜᴊ ꜱɪᴇ ᴅᴏ ᴡᴀʟᴋɪ!");
        yaml.addDefault("titles.start.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
        yaml.addDefault("titles.start.subtitle", "&cᴘᴏᴡᴏᴅᴢᴇɴɪᴀ!");
        yaml.addDefault("titles.winner.title", "&6&l🏆 {winner}");
        yaml.addDefault("titles.winner.subtitle", "&eWygrał grę!");
        yaml.addDefault("titles.loser.title", "&c&lPRZEGRAŁEŚ!");
        yaml.addDefault("titles.loser.subtitle", "&7Możesz obserwować grę lub wpisać &c/opusc");

        yaml.addDefault("actionbar.item-countdown", "&4🌞 &cʟᴏꜱᴏᴡᴀɴɪᴇ ᴘʀᴢᴇᴅᴍɪᴏᴛᴜ ᴢᴀ &4{seconds}ꜱ &c🌞");
        yaml.addDefault("actionbar.item-received", "&4✹ &cᴡʏʟᴏꜱᴏᴡᴀɴᴏ: &4{item} &c✹");
        yaml.addDefault("actionbar.spectator", "&e✦ Aby powrócić na spawna wpisz &c/opusc &e✦");

        yaml.addDefault("leave-item.name", "&c&l✖ Opuść arenę");
        yaml.addDefault("leave-item.lore", List.of("&7Kliknij PPM, aby opuścić", "&7rozgrywkę Randomizer."));

        yaml.addDefault("scoreboard.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
        yaml.addDefault("scoreboard.lines", List.of("", "&7Nick: &c{player}", "&7Teammate: &c{teammate}", "", "&4⚔ &7Zabójstwa: &c{kills}", "&c☠ &7Śmierci: &c{deaths}", "&6⌛ &7Czas: &c{minutes}min, {seconds}s", "&7Border: &c{border}", "", "&7Graczy online: &c{online}", "", "&c{website}"));

        yaml.addDefault("tab.header", List.of("", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ", "&7Aktualnie gra: &c{online} graczy", ""));
        yaml.addDefault("tab.footer", List.of("", "&c✦ &7Jesteś na trybie: &4&lRANDOMIZER &c✦", "&7Strona: &c{website} &8• &7Discord: &c{discord}", ""));

        yaml.addDefault("spawn-scoreboard.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
        yaml.addDefault("spawn-scoreboard.lines", List.of("", "&7Nick: &c{player}", "", "&6🏆 &7Wygrane: &c{wins}", "&4⚔ &7Zabójstwa: &c{kills}", "&c☠ &7Śmierci: &c{deaths}", "&6⌛ &7Czas: &c{hours}h, {minutes}min", "", "&7Graczy online: &c{online}", "", "&c{website}"));
        yaml.addDefault("spawn-tab.header", List.of("", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ", "&7Aktualnie gra: &c{online} graczy", ""));
        yaml.addDefault("spawn-tab.footer", List.of("", "&c✦ &7Jesteś na trybie: &4&lRANDOMIZER &c✦", "&7Strona: &c{website} &8• &7Discord: &c{discord}", ""));

        yaml.addDefault("menu.title", "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
        yaml.addDefault("menu.mode-name", "&c&l{mode}");
        yaml.addDefault("menu.players", "&cɢʀᴀᴄᴢᴇ: &4{players}/{max}");
        yaml.addDefault("menu.click", "&cᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴅᴏʟᴀᴄᴢʏᴄ");

        yaml.addDefault("admin.messages.no-permission", "&4&lADMIN &8» &cNie masz uprawnień.");
        yaml.addDefault("admin.messages.usage", "&4&lADMIN &8» &c/admin <list|tp <id>|playertp <nick>|stop <id>>");
        yaml.addDefault("admin.messages.arena-not-found", "&4&lADMIN &8» &cNie znaleziono areny.");
        yaml.addDefault("admin.messages.player-not-found", "&4&lADMIN &8» &cGracz nie jest online albo nie znajduje się na arenie.");
        yaml.addDefault("admin.messages.teleported", "&4&lADMIN &8» &cPrzeniesiono na arenę jako spectator.");
        yaml.addDefault("admin.messages.stopped", "&4&lADMIN &8» &cAdministrator zatrzymał arenę.");
        yaml.addDefault("admin.messages.list-header", "&4&lADMIN &8» &cAktywne areny:");
        yaml.addDefault("admin.messages.list-line", "&4#{id} &c{mode} &8• &c{players}/{max} &8• &c{state}");
        yaml.addDefault("admin.messages.no-active-arenas", "&4&lADMIN &8» &cBrak aktywnych aren.");
        yaml.addDefault("admin.titles.stopped.title", "&4&lROZGRYWKA ZATRZYMANA");
        yaml.addDefault("admin.titles.stopped.subtitle", "&cAdministrator zatrzymał Twoją arenę.");

        yaml.options().copyDefaults(true);
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Nie udało się zapisać randomizer.yml: " + exception.getMessage());
        }

        RandomizerConfig c = new RandomizerConfig();
        c.countdownSeconds = Math.max(1, yaml.getInt("countdown-seconds"));
        c.itemIntervalSeconds = Math.max(1, yaml.getInt("item-interval-seconds"));
        c.gameDurationSeconds = Math.max(60, yaml.getInt("game-duration-seconds"));
        c.worldCount = 1;
        c.arenaMinDistance = Math.max(500, yaml.getInt("worlds.arena-min-distance"));
        c.arenaMaxDistance = Math.max(c.arenaMinDistance + 500, yaml.getInt("worlds.arena-max-distance"));
        c.preloadArenaChunks = yaml.getBoolean("worlds.preload-arenas", true);
        c.keepArenaChunksLoaded = yaml.getBoolean("worlds.keep-chunks-loaded", true);
        c.preloadArenaCount = Math.max(1, Math.min(16, yaml.getInt("worlds.preload-arena-count", 1)));
        c.preloadChunksPerTick = Math.max(1, Math.min(32, yaml.getInt("worlds.preload-chunks-per-tick", 4)));
        c.borderDelaySeconds = Math.max(0, yaml.getInt("border.delay-seconds"));
        c.borderWaitingSize = Math.max(5.0, yaml.getDouble("border.waiting-size", 25.0));
        c.borderFfaWaitingSize = Math.max(5.0, yaml.getDouble("border.ffa-waiting-size", 35.0));
        c.borderGameSize = Math.max(c.borderWaitingSize, yaml.getDouble("border.game-size", 300.0));
        c.borderEndSize = Math.max(1.0, yaml.getDouble("border.end-size"));
        c.borderShrinkSeconds = Math.max(1, yaml.getInt("border.shrink-seconds"));
        c.scoreboardWebsite = yaml.getString("scoreboard.website", "www.zagrajtu.pl");
        c.scoreboardDiscord = yaml.getString("scoreboard.discord", "ᴡᴡᴡ.ᴅᴄ.ᴢᴀɢʀᴀᴊᴛᴜ.ᴘʟ");
        c.useAllVanillaItems = yaml.getBoolean("items.use-all-vanilla", true);
        c.vanillaItems = readItems(yaml, "items.vanilla");
        c.nexoItems = readItems(yaml, "items.nexo");
        c.lobby = yaml.getLocation("lobby");
        c.terrainEnabled = yaml.getBoolean("terrain.enabled", true);
        c.terrainSize = Math.max(25, yaml.getInt("terrain.size", 300));
        c.terrainDepth = Math.max(2, yaml.getInt("terrain.depth", 5));
        c.terrainClearHeight = Math.max(5, yaml.getInt("terrain.clear-height", 12));
        c.terrainMaxTrees = Math.max(0, Math.min(4, yaml.getInt("terrain.max-trees", 4)));

        c.msgAlreadyPlaying = yaml.getString("messages.already-playing", "");
        c.msgGameRunning = yaml.getString("messages.game-running", "");
        c.msgQueueFull = yaml.getString("messages.queue-full", "");
        c.msgNoArena = yaml.getString("messages.no-arena", "");
        c.msgJoin = yaml.getString("messages.join", "");
        c.msgLeave = yaml.getString("messages.leave", "");
        c.msgNotPlaying = yaml.getString("messages.not-playing", "");
        c.msgGameStarted = yaml.getString("messages.game-started", "");
        c.msgGoodLuck = yaml.getString("messages.good-luck", "");
        c.msgBorderShrink = yaml.getString("messages.border-shrink", "");
        c.msgDeath = nonBlank(yaml.getString("messages.death"), "&4☠ &c{player} &7ᴢɢɪɴᴀʟ!");
        c.msgWinnerChat = yaml.getString("messages.winner-chat", "");
        c.msgOnlyPlayers = yaml.getString("messages.only-players", "");
        c.msgUsageJoin = yaml.getString("messages.usage-join", "");
        c.msgUsageMain = yaml.getString("messages.usage-main", "");
        c.msgInvalidMode = yaml.getString("messages.invalid-mode", "");
        c.msgNoWinner = yaml.getString("messages.no-winner", "Nikt");
        c.titleWaiting = nonBlank(yaml.getString("titles.waiting.title"), "&4&lᴏᴄᴢᴇᴋɪᴡᴀɴɪᴇ...");
        c.subtitleWaiting = nonBlank(yaml.getString("titles.waiting.subtitle"), "&4🔥 &cɢʀᴀᴄᴢʏ &4{players}/{max} &8✦ &cʙʀᴀᴋᴜᴊᴇ &4{missing} {player-word} &c🔥");
        c.titleCountdown = nonBlank(yaml.getString("titles.countdown.title"), "&6&lSTART ZA {seconds}");
        c.subtitleCountdown = nonBlank(yaml.getString("titles.countdown.subtitle"), "&7Przygotuj się do walki!");
        c.titleStart = nonBlank(yaml.getString("titles.start.title"), "&6&lRANDOMIZER");
        c.subtitleStart = nonBlank(yaml.getString("titles.start.subtitle"), "&aPowodzenia!");
        c.titleWinner = nonBlank(yaml.getString("titles.winner.title"), "&6&l🏆 {winner}");
        c.subtitleWinner = nonBlank(yaml.getString("titles.winner.subtitle"), "&eWygrał grę!");
        c.titleLoser = nonBlank(yaml.getString("titles.loser.title"), "&c&lPRZEGRAŁEŚ!");
        c.subtitleLoser = nonBlank(yaml.getString("titles.loser.subtitle"), "&7Możesz obserwować grę lub wpisać &c/opusc");
        c.actionbarItemCountdown = yaml.getString("actionbar.item-countdown", "");
        c.actionbarItemReceived = yaml.getString("actionbar.item-received", "");
        c.actionbarSpectator = yaml.getString("actionbar.spectator", "");
        c.leaveItemName = yaml.getString("leave-item.name", "");
        c.leaveItemLore = new ArrayList<>(yaml.getStringList("leave-item.lore"));
        c.scoreboardTitle = nonBlank(yaml.getString("scoreboard.title"), "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
        c.scoreboardLines = new ArrayList<>(yaml.getStringList("scoreboard.lines"));
        c.tabHeader = readTextLines(yaml, "tab.header");
        c.tabFooter = readTextLines(yaml, "tab.footer");
        c.lobbyScoreboardTitle = nonBlank(yaml.getString("spawn-scoreboard.title"), "&4&lʀᴀɴᴅᴏᴍɪᴢᴇʀ");
        c.lobbyScoreboardLines = new ArrayList<>(yaml.getStringList("spawn-scoreboard.lines"));
        c.lobbyTabHeader = readTextLines(yaml, "spawn-tab.header");
        c.lobbyTabFooter = readTextLines(yaml, "spawn-tab.footer");
        c.menuTitle = yaml.getString("menu.title", "");
        c.menuModeName = yaml.getString("menu.mode-name", "");
        c.menuPlayers = yaml.getString("menu.players", "");
        c.menuClick = yaml.getString("menu.click", "");
        c.adminNoPermission = yaml.getString("admin.messages.no-permission", "");
        c.adminUsage = yaml.getString("admin.messages.usage", "");
        c.adminArenaNotFound = yaml.getString("admin.messages.arena-not-found", "");
        c.adminPlayerNotFound = yaml.getString("admin.messages.player-not-found", "");
        c.adminTeleported = yaml.getString("admin.messages.teleported", "");
        c.adminStoppedMessage = yaml.getString("admin.messages.stopped", "");
        c.adminListHeader = yaml.getString("admin.messages.list-header", "");
        c.adminListLine = yaml.getString("admin.messages.list-line", "");
        c.adminNoActiveArenas = yaml.getString("admin.messages.no-active-arenas", "");
        c.adminStopTitle = yaml.getString("admin.titles.stopped.title", "");
        c.adminStopSubtitle = yaml.getString("admin.titles.stopped.subtitle", "");
        return c;
    }

    private static List<ConfiguredItem> readItems(YamlConfiguration yaml, String path) {
        List<ConfiguredItem> result = new ArrayList<>();

        // Kompatybilność ze starym formatem:
        // items.vanilla:
        // - DIAMOND_SWORD
        List<String> oldList = yaml.getStringList(path);
        if (!oldList.isEmpty()) {
            for (String id : oldList) {
                if (id != null && !id.isBlank()) result.add(new ConfiguredItem(id, 1, 100.0));
            }
            return result;
        }

        // Nowy format:
        // items.vanilla.OAK_PLANKS.amount: 32
        // items.vanilla.OAK_PLANKS.chance: 100
        ConfigurationSection section = yaml.getConfigurationSection(path);
        if (section == null) return result;
        for (String id : section.getKeys(false)) {
            Object raw = section.get(id);
            if (raw instanceof ConfigurationSection itemSection) {
                int amount = itemSection.getInt("amount", 1);
                double chance = itemSection.getDouble("chance", 100.0);
                result.add(new ConfiguredItem(id, amount, chance));
            } else if (raw instanceof Number number) {
                result.add(new ConfiguredItem(id, number.intValue(), 100.0));
            } else if (raw instanceof Map<?, ?> map) {
                Object amountRaw = map.get("amount");
                Object chanceRaw = map.get("chance");
                int amount = amountRaw instanceof Number n ? n.intValue() : 1;
                double chance = chanceRaw instanceof Number n ? n.doubleValue() : 100.0;
                result.add(new ConfiguredItem(id, amount, chance));
            }
        }
        return result;
    }

    private static List<String> readTextLines(YamlConfiguration yaml, String path) {
        List<String> lines = yaml.getStringList(path);
        if (!lines.isEmpty()) return new ArrayList<>(lines);
        String single = yaml.getString(path, "");
        return single.isEmpty() ? new ArrayList<>() : new ArrayList<>(List.of(single));
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

}
