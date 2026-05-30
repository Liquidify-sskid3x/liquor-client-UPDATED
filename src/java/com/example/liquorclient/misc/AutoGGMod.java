package com.example.liquorclient.misc;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AutoGGMod extends Module {
    private static final List<String> TEMPLATES = List.of(
            "good game {player}",
            "gg {player}!",
            "good job {player}!",
            "gg!",
            "well played {player}"
    );

    private final NumberSetting cooldownSeconds = new NumberSetting("Cooldown Seconds", 3.0, 0.0, 20.0);
    private long lastSendTime = 0L;

    public AutoGGMod() {
        super("Auto GG", Category.MISC, "Automatically says GG after a game ends");
        addSetting(cooldownSeconds);
    }

    public void onGameMessage(String message) {
        if (!isEnabled() || message == null || message.isBlank()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.networkHandler == null) return;

        String self = mc.player.getName().getString();
        String victim = findVictim(message, self);
        if (victim == null || victim.equalsIgnoreCase(self)) return;

        long now = System.currentTimeMillis();
        long cooldownMs = Math.round(cooldownSeconds.getValue() * 1000.0);
        if (now - lastSendTime < cooldownMs) return;
        lastSendTime = now;

        String template = TEMPLATES.get(ThreadLocalRandom.current().nextInt(TEMPLATES.size()));
        String chat = template.replace("{player}", victim);
        mc.execute(() -> {
            if (mc.player != null && mc.player.networkHandler != null) {
                mc.player.networkHandler.sendChatMessage(chat);
            }
        });
    }

    private String findVictim(String message, String self) {
        String[] markers = {
                " was slain by " + self,
                " was shot by " + self,
                " was killed by " + self,
                " was blown up by " + self,
                " was fireballed by " + self,
                " was pummeled by " + self,
                " was poked to death by " + self
        };

        for (String marker : markers) {
            int idx = message.indexOf(marker);
            if (idx > 0) {
                return cleanName(message.substring(0, idx));
            }
        }

        String escapeMarker = " while trying to escape " + self;
        int escapeIdx = message.indexOf(escapeMarker);
        if (escapeIdx > 0) {
            int wasIdx = message.indexOf(" was ");
            return cleanName(wasIdx > 0 ? message.substring(0, wasIdx) : message.substring(0, escapeIdx));
        }

        return null;
    }

    private String cleanName(String value) {
        String cleaned = value == null ? "" : value.trim();
        int colon = cleaned.lastIndexOf(':');
        if (colon >= 0 && colon < cleaned.length() - 1) {
            cleaned = cleaned.substring(colon + 1).trim();
        }
        return cleaned.isBlank() ? null : cleaned;
    }
}
