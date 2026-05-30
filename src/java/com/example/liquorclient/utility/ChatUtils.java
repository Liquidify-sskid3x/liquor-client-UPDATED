package com.example.liquorclient.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatUtils {
    private static final String PREFIX = Formatting.GRAY + "[" + Formatting.DARK_PURPLE + "Liquor" + Formatting.GRAY + "] " + Formatting.RESET;

    public static void message(String msg) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal(PREFIX + msg), false);
        }
    }

    public static void error(String msg) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal(PREFIX + Formatting.RED + "Error: " + Formatting.GRAY + msg), false);
        }
    }

    public static void warning(String msg) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal(PREFIX + Formatting.YELLOW + "Warning: " + Formatting.GRAY + msg), false);
        }
    }

    public static void info(String msg) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal(PREFIX + Formatting.AQUA + "Info: " + Formatting.GRAY + msg), false);
        }
    }
}
