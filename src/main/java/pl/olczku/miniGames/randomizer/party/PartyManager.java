package pl.olczku.miniGames.randomizer.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.olczku.miniGames.randomizer.game.RandomizerMode;
import pl.olczku.miniGames.randomizer.game.RandomizerService;
import pl.olczku.miniGames.randomizer.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PartyManager {
    private static final int MAX_PARTY_SIZE = 4;
    private static final long INVITE_LIFETIME_MILLIS = 60_000L;

    private final RandomizerService service;
    private final Map<UUID, Party> byMember = new HashMap<>();
    private final Map<UUID, Invite> invites = new HashMap<>();

    public PartyManager(RandomizerService service) {
        this.service = service;
    }

    public List<Player> onlineParty(Player leader) {
        Party party = byMember.get(leader.getUniqueId());
        if (party == null) return List.of(leader);
        if (!party.leader.equals(leader.getUniqueId())) return List.of();

        return party.members.stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .filter(Player::isOnline)
            .toList();
    }

    public void handle(Player player, String[] args) {
        cleanupExpiredInvites();

        if (args.length == 0) {
            help(player);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "pomoc", "help" -> help(player);
            case "zapros", "invite" -> invite(player, args);
            case "dolacz", "accept" -> accept(player);
            case "odrzuc", "deny" -> deny(player);
            case "wyrzuc", "kick" -> kick(player, args);
            case "opusc", "leave" -> leave(player);
            case "lista", "list" -> list(player);
            case "lider", "leader" -> transferLeader(player, args);
            default -> msg(player, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇᴘʀᴀᴡɪᴅʟᴏᴡᴀ ᴋᴏᴍᴇɴᴅᴀ. &7ᴜᴢʏᴊ &c/party pomoc&7.");
        }
    }

    public boolean validateMode(Player leader, RandomizerMode mode) {
        Party party = byMember.get(leader.getUniqueId());
        if (party != null && !party.leader.equals(leader.getUniqueId())) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴛʏʟᴋᴏ ʟɪᴅᴇʀ ᴍᴏᴢᴇ ᴅᴏʟᴀᴄᴢʏᴄ ᴘᴀʀᴛʏ ᴅᴏ ɢʀʏ.");
            return false;
        }

        List<Player> members = onlineParty(leader);
        if (members.isEmpty()) return false;

        int maxParty = mode == RandomizerMode.FFA ? 1 : mode.teamSize();
        if (members.size() > maxParty) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴘᴀʀᴛʏ ᴊᴇꜱᴛ ᴢᴀ ᴅᴜᴢᴇ ᴅʟᴀ ᴛʀʏʙᴜ &4" + mode.id() + "&c. &7ᴍᴀᴋꜱ: &c" + maxParty + "&7.");
            return false;
        }

        for (Player member : members) {
            if (service.modeOf(member.getUniqueId()) != null) {
                msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cɢʀᴀᴄᴢ &4" + member.getName() + " &cᴊᴇꜱᴛ ᴊᴜᴢ ᴡ ᴋᴏʟᴇᴊᴄᴇ ʟᴜʙ ɢʀᴢᴇ.");
                return false;
            }
        }
        return true;
    }

    public void onQuit(Player player) {
        invites.remove(player.getUniqueId());
        invites.entrySet().removeIf(entry -> entry.getValue().leaderId.equals(player.getUniqueId()));
    }

    private void help(Player player) {
        player.sendMessage(Text.mm("&4&lᴘᴀʀᴛʏ &8» &7ᴅᴏꜱᴛᴇᴘɴᴇ ᴋᴏᴍᴇɴᴅʏ:"));
        player.sendMessage(Text.mm("&c/party zapros <gracz> &8- &7ᴢᴀᴘʀᴀꜱᴢᴀ ɢʀᴀᴄᴢᴀ"));
        player.sendMessage(Text.mm("&c/party dolacz &8- &7ᴀᴋᴄᴇᴘᴛᴜᴊᴇ ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴇ"));
        player.sendMessage(Text.mm("&c/party odrzuc &8- &7ᴏᴅʀᴢᴜᴄᴀ ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴇ"));
        player.sendMessage(Text.mm("&c/party wyrzuc <gracz> &8- &7ᴜꜱᴜᴡᴀ ᴢ ᴘᴀʀᴛʏ"));
        player.sendMessage(Text.mm("&c/party lider <gracz> &8- &7ᴘʀᴢᴇᴋᴀᴢᴜᴊᴇ ʟɪᴅᴇʀᴀ"));
        player.sendMessage(Text.mm("&c/party lista &8- &7ᴘᴏᴋᴀᴢᴜᴊᴇ ꜱᴋʟᴀᴅ"));
        player.sendMessage(Text.mm("&c/party opusc &8- &7ᴏᴘᴜꜱᴢᴄᴢᴀ ᴘᴀʀᴛʏ"));
    }

    private void invite(Player leader, String[] args) {
        if (service.modeOf(leader.getUniqueId()) != null) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴍᴏᴢᴇꜱᴢ ᴢᴍɪᴇɴɪᴀᴄ ᴘᴀʀᴛʏ ᴘᴏᴅᴄᴢᴀꜱ ɢʀʏ.");
            return;
        }
        if (args.length < 2) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴜᴢʏᴄɪᴇ: /party zapros <gracz>");
            return;
        }

        Party party = byMember.computeIfAbsent(leader.getUniqueId(), Party::new);
        if (!party.leader.equals(leader.getUniqueId())) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴛʏʟᴋᴏ ʟɪᴅᴇʀ ᴍᴏᴢᴇ ᴢᴀᴘʀᴀꜱᴢᴀᴄ.");
            return;
        }
        if (party.members.size() >= MAX_PARTY_SIZE) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴘᴀʀᴛʏ ᴊᴇꜱᴛ ᴘᴇʟɴᴇ.");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴢɴᴀʟᴇᴢɪᴏɴᴏ ɢʀᴀᴄᴢᴀ.");
            return;
        }
        if (target.equals(leader)) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴍᴏᴢᴇꜱᴢ ᴢᴀᴘʀᴏꜱɪᴄ ꜱᴀᴍᴇɢᴏ ꜱɪᴇʙɪᴇ.");
            return;
        }
        if (byMember.containsKey(target.getUniqueId())) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴛᴇɴ ɢʀᴀᴄᴢ ᴊᴇꜱᴛ ᴊᴜᴢ ᴡ ᴘᴀʀᴛʏ.");
            return;
        }
        if (service.modeOf(target.getUniqueId()) != null) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴛᴇɴ ɢʀᴀᴄᴢ ᴊᴇꜱᴛ ᴡ ᴋᴏʟᴇᴊᴄᴇ ʟᴜʙ ɢʀᴢᴇ.");
            return;
        }

        invites.put(target.getUniqueId(), new Invite(leader.getUniqueId(), System.currentTimeMillis() + INVITE_LIFETIME_MILLIS));
        msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴢᴀᴘʀᴏꜱᴢᴏɴᴏ &4" + target.getName() + "&c.");
        msg(target, "&4&lᴘᴀʀᴛʏ &8» &c" + leader.getName() + " &7ᴢᴀᴘʀᴀꜱᴢᴀ ᴄɪᴇ ᴅᴏ ᴘᴀʀᴛʏ.");
        msg(target, "&7ᴡᴘɪꜱᴢ &c/party dolacz &7ʟᴜʙ &c/party odrzuc&7. ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴇ ᴡʏɢᴀꜱᴀ ᴘᴏ 60 ꜱᴇᴋᴜɴᴅᴀᴄʜ.");
    }

    private void accept(Player player) {
        if (service.modeOf(player.getUniqueId()) != null) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴍᴏᴢᴇꜱᴢ ᴅᴏʟᴀᴄᴢʏᴄ ᴅᴏ ᴘᴀʀᴛʏ ᴘᴏᴅᴄᴢᴀꜱ ɢʀʏ.");
            return;
        }

        Invite invite = invites.remove(player.getUniqueId());
        if (invite == null || invite.expiresAt < System.currentTimeMillis()) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴍᴀꜱᴢ ᴀᴋᴛʏᴡɴᴇɢᴏ ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴀ.");
            return;
        }

        Player leader = Bukkit.getPlayer(invite.leaderId);
        if (leader == null || !leader.isOnline()) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cʟɪᴅᴇʀ ᴘᴀʀᴛʏ ᴊᴇꜱᴛ ᴏꜰꜰʟɪɴᴇ.");
            return;
        }

        Party party = byMember.computeIfAbsent(invite.leaderId, Party::new);
        if (!party.leader.equals(invite.leaderId)) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cᴛᴏ ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴇ ᴊᴜᴢ ɴɪᴇ ᴊᴇꜱᴛ ᴡᴀᴢɴᴇ.");
            return;
        }
        if (party.members.size() >= MAX_PARTY_SIZE) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cᴘᴀʀᴛʏ ᴊᴇꜱᴛ ᴘᴇʟɴᴇ.");
            return;
        }

        removeFromCurrentParty(player.getUniqueId(), false);
        party.members.add(player.getUniqueId());
        byMember.put(player.getUniqueId(), party);
        broadcast(party, "&4&lᴘᴀʀᴛʏ &8» &c" + player.getName() + " &7ᴅᴏʟᴀᴄᴢʏʟ ᴅᴏ ᴘᴀʀᴛʏ.");
    }

    private void deny(Player player) {
        Invite invite = invites.remove(player.getUniqueId());
        if (invite == null) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴍᴀꜱᴢ ᴀᴋᴛʏᴡɴᴇɢᴏ ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴀ.");
            return;
        }
        msg(player, "&4&lᴘᴀʀᴛʏ &8» &cᴏᴅʀᴢᴜᴄᴏɴᴏ ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴇ.");
        Player leader = Bukkit.getPlayer(invite.leaderId);
        if (leader != null) msg(leader, "&4&lᴘᴀʀᴛʏ &8» &c" + player.getName() + " &7ᴏᴅʀᴢᴜᴄɪʟ ᴢᴀᴘʀᴏꜱᴢᴇɴɪᴇ.");
    }

    private void kick(Player leader, String[] args) {
        Party party = byMember.get(leader.getUniqueId());
        if (party == null || !party.leader.equals(leader.getUniqueId())) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴊᴇꜱᴛᴇꜱ ʟɪᴅᴇʀᴇᴍ ᴘᴀʀᴛʏ.");
            return;
        }
        if (service.modeOf(leader.getUniqueId()) != null) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴍᴏᴢᴇꜱᴢ ᴢᴍɪᴇɴɪᴀᴄ ᴘᴀʀᴛʏ ᴘᴏᴅᴄᴢᴀꜱ ɢʀʏ.");
            return;
        }
        if (args.length < 2) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴜᴢʏᴄɪᴇ: /party wyrzuc <gracz>");
            return;
        }

        UUID targetId = findMemberId(party, args[1]);
        if (targetId == null || targetId.equals(leader.getUniqueId())) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴛᴇɴ ɢʀᴀᴄᴢ ɴɪᴇ ᴊᴇꜱᴛ ᴡ ᴛᴡᴏɪᴍ ᴘᴀʀᴛʏ.");
            return;
        }

        party.members.remove(targetId);
        byMember.remove(targetId);
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) msg(target, "&4&lᴘᴀʀᴛʏ &8» &cᴢᴏꜱᴛᴀʟᴇꜱ ᴡʏʀᴢᴜᴄᴏɴʏ ᴢ ᴘᴀʀᴛʏ.");
        broadcast(party, "&4&lᴘᴀʀᴛʏ &8» &c" + nameOf(targetId) + " &7ᴢᴏꜱᴛᴀʟ ᴡʏʀᴢᴜᴄᴏɴʏ ᴢ ᴘᴀʀᴛʏ.");
    }

    private void leave(Player player) {
        Party party = byMember.get(player.getUniqueId());
        if (party == null) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴊᴇꜱᴛᴇꜱ ᴡ ᴘᴀʀᴛʏ.");
            return;
        }
        if (service.modeOf(player.getUniqueId()) != null) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴍᴏᴢᴇꜱᴢ ᴏᴘᴜꜱᴄɪᴄ ᴘᴀʀᴛʏ ᴘᴏᴅᴄᴢᴀꜱ ɢʀʏ.");
            return;
        }

        if (party.leader.equals(player.getUniqueId())) {
            List<UUID> members = new ArrayList<>(party.members);
            broadcast(party, "&4&lᴘᴀʀᴛʏ &8» &cᴘᴀʀᴛʏ ᴢᴏꜱᴛᴀʟᴏ ʀᴏᴢᴡɪᴀᴢᴀɴᴇ.");
            for (UUID id : members) byMember.remove(id);
        } else {
            party.members.remove(player.getUniqueId());
            byMember.remove(player.getUniqueId());
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &cᴏᴘᴜꜱᴄɪʟᴇꜱ ᴘᴀʀᴛʏ.");
            broadcast(party, "&4&lᴘᴀʀᴛʏ &8» &c" + player.getName() + " &7ᴏᴘᴜꜱᴄɪʟ ᴘᴀʀᴛʏ.");
        }
    }

    private void list(Player player) {
        Party party = byMember.get(player.getUniqueId());
        if (party == null) {
            msg(player, "&4&lᴘᴀʀᴛʏ &8» &7ɴɪᴇ ᴊᴇꜱᴛᴇꜱ ᴡ ᴘᴀʀᴛʏ.");
            return;
        }

        msg(player, "&4&lᴘᴀʀᴛʏ &8» &7ᴄᴢʟᴏɴᴋᴏᴡɪᴇ &c(" + party.members.size() + "/" + MAX_PARTY_SIZE + ")&7:");
        for (UUID id : party.members) {
            String prefix = id.equals(party.leader) ? "&4★ &c" : "&8- &7";
            Player member = Bukkit.getPlayer(id);
            String status = member != null && member.isOnline() ? " &aᴏɴʟɪɴᴇ" : " &cᴏꜰꜰʟɪɴᴇ";
            player.sendMessage(Text.mm(prefix + nameOf(id) + status));
        }
    }

    private void transferLeader(Player leader, String[] args) {
        Party party = byMember.get(leader.getUniqueId());
        if (party == null || !party.leader.equals(leader.getUniqueId())) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cɴɪᴇ ᴊᴇꜱᴛᴇꜱ ʟɪᴅᴇʀᴇᴍ ᴘᴀʀᴛʏ.");
            return;
        }
        if (args.length < 2) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴜᴢʏᴄɪᴇ: /party lider <gracz>");
            return;
        }
        UUID targetId = findMemberId(party, args[1]);
        if (targetId == null || targetId.equals(leader.getUniqueId())) {
            msg(leader, "&4&lᴘᴀʀᴛʏ &8» &cᴡʏʙɪᴇʀᴢ ɪɴɴᴇɢᴏ ᴄᴢʟᴏɴᴋᴀ ᴘᴀʀᴛʏ.");
            return;
        }

        party.leader = targetId;
        broadcast(party, "&4&lᴘᴀʀᴛʏ &8» &7ɴᴏᴡʏ ʟɪᴅᴇʀ: &c" + nameOf(targetId) + "&7.");
    }

    private void removeFromCurrentParty(UUID id, boolean notify) {
        Party party = byMember.remove(id);
        if (party == null) return;
        party.members.remove(id);
        if (notify) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) msg(player, "&4&lᴘᴀʀᴛʏ &8» &cᴏᴘᴜꜱᴄɪʟᴇꜱ ᴘᴀʀᴛʏ.");
        }
        if (party.members.isEmpty()) return;
        if (party.leader.equals(id)) {
            party.leader = party.members.iterator().next();
            broadcast(party, "&4&lᴘᴀʀᴛʏ &8» &7ɴᴏᴡʏ ʟɪᴅᴇʀ: &c" + nameOf(party.leader) + "&7.");
        }
    }

    private UUID findMemberId(Party party, String name) {
        for (UUID id : party.members) {
            if (nameOf(id).equalsIgnoreCase(name)) return id;
        }
        return null;
    }

    private String nameOf(UUID id) {
        Player online = Bukkit.getPlayer(id);
        if (online != null) return online.getName();
        String offlineName = Bukkit.getOfflinePlayer(id).getName();
        return offlineName == null ? id.toString().substring(0, 8) : offlineName;
    }

    private void cleanupExpiredInvites() {
        long now = System.currentTimeMillis();
        invites.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
    }

    private void broadcast(Party party, String text) {
        for (UUID id : party.members) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) msg(player, text);
        }
    }

    private void msg(Player player, String text) {
        player.sendMessage(Text.mm(text));
    }

    private static final class Invite {
        private final UUID leaderId;
        private final long expiresAt;

        private Invite(UUID leaderId, long expiresAt) {
            this.leaderId = leaderId;
            this.expiresAt = expiresAt;
        }
    }

    private static final class Party {
        private UUID leader;
        private final LinkedHashSet<UUID> members = new LinkedHashSet<>();

        private Party(UUID leader) {
            this.leader = leader;
            this.members.add(leader);
        }
    }
}
