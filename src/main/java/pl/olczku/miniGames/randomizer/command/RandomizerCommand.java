package pl.olczku.miniGames.randomizer.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Sender;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.entity.Player;
import pl.olczku.miniGames.randomizer.game.RandomizerMode;
import pl.olczku.miniGames.randomizer.game.RandomizerService;

@Command(name = "randomizer", aliases = "rand")
public final class RandomizerCommand {
    private final RandomizerService service;

    public RandomizerCommand(RandomizerService service) {
        this.service = service;
    }

    @Execute
    void menu(@Sender Player player) {
        service.openMenu(player);
    }

    @Execute(name = "dolacz")
    void join(@Sender Player player, @Arg String mode) {
        try {
            service.join(player, RandomizerMode.parse(mode));
        } catch (IllegalArgumentException exception) {
            player.sendMessage(exception.getMessage());
        }
    }

    @Execute(name = "opusc")
    void leave(@Sender Player player) {
        service.leave(player);
    }
}
