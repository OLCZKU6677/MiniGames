package pl.olczku.miniGames.randomizer.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.projectiles.ProjectileSource;
import pl.olczku.miniGames.randomizer.game.RandomizerService;

public final class RandomizerListener implements Listener {
    private final RandomizerService service;

    public RandomizerListener(RandomizerService service) {
        this.service = service;
    }


    @EventHandler
    public void join(PlayerJoinEvent event) {
        service.onPlayerJoin(event.getPlayer());
    }



    @EventHandler
    public void move(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;
        service.pushApartOnArena(event.getPlayer(), event.getTo());
    }

    @EventHandler
    public void changedWorld(PlayerChangedWorldEvent event) {
        service.scheduleLobbyVisuals(event.getPlayer());
        service.refreshPlayerVisibility();
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event) {
        if (service.modeOf(event.getPlayer().getUniqueId()) == null) {
            service.scheduleLobbyVisuals(event.getPlayer());
        }
    }



    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        event.getRecipients().removeIf(target -> !service.canSeeOrChat(sender, target));
    }

    @EventHandler
    public void weatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState() && service.isRandomizerWorld(event.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void thunderChange(ThunderChangeEvent event) {
        if (event.toThunderState() && service.isRandomizerWorld(event.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) {
        if (service.isRandomizerWorld(event.getLocation().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preGameDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && service.isPreGame(player)) event.setCancelled(true);
    }

    @EventHandler
    public void pvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = attackingPlayer(event);
        if (attacker == null) return;
        if (service.isPreGame(victim) || service.isPreGame(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (service.modeOf(event.getPlayer().getUniqueId()) == null) return;
        if (service.isPreGame(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        event.setDropItems(false);
        event.setExpToDrop(0);
    }

    @EventHandler
    public void pickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (service.modeOf(player.getUniqueId()) != null) event.setCancelled(true);
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (service.isPreGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        if (service.modeOf(event.getEntity().getUniqueId()) == null) return;
        event.deathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);
        service.onDeath(event.getEntity());
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        Location location = service.respawnLocation(event.getPlayer());
        if (location == null) return;
        event.setRespawnLocation(location);
        service.onRespawn(event.getPlayer());
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (!service.handleLeaveItem(event.getPlayer(), event.getItem())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void drop(PlayerDropItemEvent event) {
        if (service.modeOf(event.getPlayer().getUniqueId()) == null) return;
        if (service.handleLeaveItem(event.getPlayer(), event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void exposePartyCommands(PlayerCommandSendEvent event) {
        event.getCommands().add("party");
        event.getCommands().add("p");
        event.getCommands().add("opusc");
        if (event.getPlayer().hasPermission("randomizer.admin")) event.getCommands().add("admin");
    }

    @EventHandler
    public void partyTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) return;

        String buffer = event.getBuffer().trim().startsWith("/")
            ? event.getBuffer().substring(1)
            : event.getBuffer();
        String[] parts = buffer.split("\\s+", -1);
        if (parts.length == 0) return;

        String root = parts[0].toLowerCase(java.util.Locale.ROOT);

        // Podpowiadanie samych komend po wpisaniu "/" lub ich początku.
        if (parts.length == 1) {
            java.util.List<String> roots = player.hasPermission("randomizer.admin")
                ? java.util.List.of("admin", "opusc", "party", "p", "randomizer")
                : java.util.List.of("opusc", "party", "p", "randomizer");
            event.setCompletions(roots.stream()
                .filter(option -> option.startsWith(root))
                .toList());
            return;
        }

        if (root.equals("admin") && player.hasPermission("randomizer.admin")) {
            java.util.List<String> adminOptions;
            if (parts.length <= 2) {
                adminOptions = java.util.List.of("list", "tp", "playertp", "stop");
            } else if (parts.length == 3 && parts[1].equalsIgnoreCase("playertp")) {
                adminOptions = org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
            } else {
                adminOptions = java.util.List.of();
            }
            String typedAdmin = parts[parts.length - 1].toLowerCase(java.util.Locale.ROOT);
            event.setCompletions(adminOptions.stream().filter(option -> option.toLowerCase(java.util.Locale.ROOT).startsWith(typedAdmin)).toList());
            return;
        }

        if (!root.equals("party") && !root.equals("p")) return;

        java.util.List<String> options;
        if (parts.length <= 2) {
            options = java.util.List.of("zapros", "dolacz", "odrzuc", "wyrzuc", "opusc", "lista", "lider", "pomoc");
        } else if (parts.length == 3 && (
            parts[1].equalsIgnoreCase("zapros")
                || parts[1].equalsIgnoreCase("dolacz")
                || parts[1].equalsIgnoreCase("wyrzuc")
                || parts[1].equalsIgnoreCase("lider")
        )) {
            options = org.bukkit.Bukkit.getOnlinePlayers().stream()
                .filter(target -> !target.getUniqueId().equals(player.getUniqueId()))
                .map(Player::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        } else {
            options = java.util.List.of();
        }

        String typed = parts[parts.length - 1].toLowerCase(java.util.Locale.ROOT);
        event.setCompletions(options.stream()
            .filter(option -> option.toLowerCase(java.util.Locale.ROOT).startsWith(typed))
            .toList());
    }

    @EventHandler
    public void command(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().trim();
        if (message.equalsIgnoreCase("/opusc")) {
            event.setCancelled(true);
            service.leave(event.getPlayer());
            return;
        }

        String lower = message.toLowerCase(java.util.Locale.ROOT);
        if (lower.equals("/party") || lower.startsWith("/party ") || lower.equals("/p") || lower.startsWith("/p ")) {
            event.setCancelled(true);
            int commandLength = lower.startsWith("/party") ? 6 : 2;
            String raw = message.length() > commandLength ? message.substring(commandLength).trim() : "";
            service.handlePartyCommand(event.getPlayer(), raw.isEmpty() ? new String[0] : raw.split("\\s+"));
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        service.onPlayerQuit(event.getPlayer());
        service.handlePartyQuit(event.getPlayer());
        if (service.modeOf(event.getPlayer().getUniqueId()) != null) service.leave(event.getPlayer());
    }

    private Player attackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) return player;
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) return player;
        }
        return null;
    }
}
