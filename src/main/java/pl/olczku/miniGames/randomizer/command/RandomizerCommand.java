package pl.olczku.miniGames.randomizer.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.olczku.miniGames.randomizer.game.RandomizerMode;
import pl.olczku.miniGames.randomizer.game.RandomizerService;
import pl.olczku.miniGames.randomizer.util.Text;

import java.util.List;

public final class RandomizerCommand implements CommandExecutor, TabCompleter {
    private final RandomizerService service;

    public RandomizerCommand(RandomizerService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.mm(service.config().msgOnlyPlayers));
            return true;
        }

        if (args.length == 0) {
            service.openMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("opusc")) {
            service.leave(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("party")) {
            String[] partyArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
            service.handlePartyCommand(player, partyArgs);
            return true;
        }

        if (args[0].equalsIgnoreCase("dolacz")) {
            if (args.length < 2) {
                player.sendMessage(Text.mm(service.config().msgUsageJoin));
                return true;
            }
            try {
                service.join(player, RandomizerMode.parse(args[1]));
            } catch (IllegalArgumentException exception) {
                player.sendMessage(Text.mm(service.config().msgInvalidMode));
            }
            return true;
        }

        player.sendMessage(Text.mm(service.config().msgUsageMain));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("dolacz", "opusc", "party");
        if (args.length == 2 && args[0].equalsIgnoreCase("dolacz")) return List.of("1v1", "2v2", "4v4", "ffa");
        if (args[0].equalsIgnoreCase("party")) {
            if (args.length == 2) return List.of("zapros", "dolacz", "odrzuc", "wyrzuc", "opusc", "lista", "lider");
            if (args.length == 3 && (args[1].equalsIgnoreCase("zapros") || args[1].equalsIgnoreCase("wyrzuc") || args[1].equalsIgnoreCase("lider"))) {
                return org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        return List.of();
    }
}
