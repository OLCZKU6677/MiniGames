package pl.olczku.miniGames.randomizer.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.Map;

public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Text() {}

    public static Component mm(String text) {
        return LEGACY.deserialize(text == null ? "" : text);
    }

    public static Component mm(String text, Map<String, String> placeholders) {
        return mm(replace(text, placeholders));
    }

    public static String legacy(String text, Map<String, String> placeholders) {
        return ChatColor.translateAlternateColorCodes('&', replace(text, placeholders));
    }

    private static String replace(String text, Map<String, String> placeholders) {
        String result = text == null ? "" : text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
