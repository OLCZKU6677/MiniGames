package pl.olczku.miniGames.randomizer.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private Text() {}

    public static Component mm(String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    public static String small(String text) {
        String normal = "abcdefghijklmnopqrstuvwxyz";
        String small = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";
        StringBuilder output = new StringBuilder();
        for (char character : text.toLowerCase().toCharArray()) {
            int index = normal.indexOf(character);
            output.append(index >= 0 ? small.charAt(index) : character);
        }
        return output.toString();
    }
}
