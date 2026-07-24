package pl.olczku.miniGames.randomizer.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.olczku.miniGames.randomizer.game.RandomizerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class StandaloneCommands {
    private StandaloneCommands() {}

    public static void register(JavaPlugin plugin, RandomizerService service) {
        var commandMap = plugin.getServer().getCommandMap();
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), new PartyRootCommand(service));
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), new LeaveRootCommand(service));
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), new AdminRootCommand(service));
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                plugin.getServer().getCommandMap();
            } catch (Exception ignored) {
            }
        });
    }

    private static final class PartyRootCommand extends Command {
        private final RandomizerService service;

        private PartyRootCommand(RandomizerService service) {
            super("party", "Komendy party", "/party", List.of("p"));
            this.service = service;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (!(sender instanceof Player player)) return true;
            service.handlePartyCommand(player, args);
            return true;
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            if (!(sender instanceof Player player)) return List.of();
            if (args.length == 1) return filter(List.of("zapros", "dolacz", "odrzuc", "wyrzuc", "opusc", "lista", "lider", "pomoc"), args[0]);
            if (args.length == 2 && List.of("zapros", "dolacz", "wyrzuc", "lider").contains(args[0].toLowerCase(Locale.ROOT))) {
                List<String> names = new ArrayList<>();
                for (Player target : Bukkit.getOnlinePlayers()) if (!target.equals(player)) names.add(target.getName());
                return filter(names, args[1]);
            }
            return List.of();
        }
    }

    private static final class AdminRootCommand extends Command {
        private final RandomizerService service;

        private AdminRootCommand(RandomizerService service) {
            super("admin", "Administracja arenami Randomizer", "/admin <list|tp|playertp|stop>", List.of());
            this.service = service;
            setPermission("randomizer.admin");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (!sender.hasPermission("randomizer.admin")) {
                sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminNoPermission));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminUsage));
                return true;
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("list")) {
                List<String> arenas = service.adminArenaList();
                sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminListHeader));
                if (arenas.isEmpty()) {
                    sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminNoActiveArenas));
                    return true;
                }
                for (String raw : arenas) {
                    String[] parts = raw.split("\\|");
                    java.util.Map<String, String> ph = java.util.Map.of(
                        "id", parts[0], "mode", parts[1], "players", parts[2], "max", parts[3], "state", parts[4]
                    );
                    sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminListLine, ph));
                }
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().msgOnlyPlayers));
                return true;
            }
            if (sub.equals("tp") || sub.equals("stop")) {
                if (args.length < 2) {
                    sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminUsage));
                    return true;
                }
                int id;
                try { id = Integer.parseInt(args[1]); }
                catch (NumberFormatException ex) {
                    sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminArenaNotFound));
                    return true;
                }
                boolean ok = sub.equals("tp") ? service.adminTeleportToArena(player, id) : service.adminStopArena(id);
                sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(ok ? (sub.equals("tp") ? service.config().adminTeleported : service.config().adminStoppedMessage) : service.config().adminArenaNotFound));
                return true;
            }
            if (sub.equals("playertp")) {
                if (args.length < 2) {
                    sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminUsage));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !service.adminTeleportToPlayer(player, target)) {
                    sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminPlayerNotFound));
                } else {
                    sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminTeleported));
                }
                return true;
            }
            sender.sendMessage(pl.olczku.miniGames.randomizer.util.Text.mm(service.config().adminUsage));
            return true;
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            if (!sender.hasPermission("randomizer.admin")) return List.of();
            if (args.length == 1) return filter(List.of("list", "tp", "playertp", "stop"), args[0]);
            if (args.length == 2 && args[0].equalsIgnoreCase("playertp")) {
                return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("stop"))) {
                List<String> ids = new ArrayList<>();
                for (int i = 1; i <= service.adminArenaList().size(); i++) ids.add(String.valueOf(i));
                return filter(ids, args[1]);
            }
            return List.of();
        }
    }

    private static final class LeaveRootCommand extends Command {
        private final RandomizerService service;
        private LeaveRootCommand(RandomizerService service) { super("opusc", "Opuszcza Randomizer", "/opusc", List.of()); this.service = service; }
        @Override public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) service.leave(player);
            return true;
        }
    }

    private static List<String> filter(List<String> values, String typed) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        return values.stream().filter(v -> v.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
