package com.example.liquorclient.command;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.gui.DeveloperScreen;
import com.example.liquorclient.gui.NotificationManager;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.ColorSetting;
import com.example.liquorclient.module.KeybindSetting;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class CommandManager {
    private static final String PREFIX = ".";

    private CommandManager() {
    }

    public static boolean handleChatMessage(String message) {
        if (message == null || !message.startsWith(PREFIX) || message.equals(PREFIX)) return false;

        String raw = message.substring(PREFIX.length()).trim();
        if (raw.isEmpty()) return true;

        String[] parts = raw.split("\\s+");
        String command = parts[0].toLowerCase(Locale.ROOT);
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        try {
            switch (command) {
                case "help" -> help();
                case "toggle", "t" -> toggle(args);
                case "bind" -> bind(args);
                case "set" -> set(args);
                case "modules" -> modules();
                case "profile" -> profile(args);
                case "config" -> config(args);
                case "dev", "developer" -> openDeveloperScreen();
                default -> reply("Unknown command. Try .help");
            }
        } catch (RuntimeException ex) {
            reply("Command failed: " + ex.getMessage());
        }

        return true;
    }

    private static void help() {
        reply(".modules, .toggle <module>, .bind <module> <key|none>");
        reply(".set <module> <setting> <value>, .profile <list|use|create|delete|export|import>, .config save, .dev");
    }

    private static void modules() {
        String joined = ModuleManager.getModules().stream()
                .map(module -> module.getName() + (module.isEnabled() ? "[on]" : "[off]"))
                .reduce((left, right) -> left + ", " + right)
                .orElse("No modules");
        reply(joined);
    }

    private static void toggle(String[] args) {
        Module module = findModule(args, 0);
        if (module == null) {
            reply("Usage: .toggle <module>");
            return;
        }
        module.toggle();
        reply(module.getName() + " is " + (module.isEnabled() ? "on" : "off"));
    }

    private static void bind(String[] args) {
        if (args.length < 2) {
            reply("Usage: .bind <module> <key|none>");
            return;
        }
        Module module = findModule(args, 0, args.length - 1);
        if (module == null) {
            reply("Module not found");
            return;
        }
        int key = parseKey(args[args.length - 1]);
        module.getKeybind().setValue(key);
        reply(module.getName() + " bound to " + com.example.liquorclient.gui.ClickGui.getKeyName(key));
    }

    private static void set(String[] args) {
        if (args.length < 3) {
            reply("Usage: .set <module> <setting> <value>");
            return;
        }

        Module module = null;
        Setting<?> setting = null;
        int valueIndex = -1;

        for (int i = 1; i < args.length; i++) {
            Module candidate = findModule(args, 0, i);
            if (candidate == null) continue;

            for (int j = i + 1; j <= args.length; j++) {
                Setting<?> found = findSetting(candidate, join(args, i, j));
                if (found != null && j < args.length) {
                    module = candidate;
                    setting = found;
                    valueIndex = j;
                }
            }
        }

        if (module == null || setting == null || valueIndex < 0) {
            reply("Module or setting not found");
            return;
        }

        String value = join(args, valueIndex, args.length);
        applySetting(setting, value);
        reply(module.getName() + " " + setting.getName() + " = " + setting.getValue());
    }

    private static void profile(String[] args) {
        if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
            reply("Profiles: " + String.join(", ", ConfigManager.getProfiles()));
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        String name = args.length > 1 ? args[1] : "";
        switch (action) {
            case "use", "switch" -> reply(ConfigManager.switchProfile(name) ? "Loaded " + name : "Could not load profile");
            case "create" -> reply(ConfigManager.createProfile(name) ? "Created " + name : "Could not create profile");
            case "delete" -> reply(ConfigManager.deleteProfile(name) ? "Deleted " + name : "Could not delete profile");
            case "export" -> exportProfile();
            case "import" -> importProfile(name);
            default -> reply("Usage: .profile <list|use|create|delete|export|import>");
        }
    }

    private static void config(String[] args) {
        if (args.length == 0 || !"save".equalsIgnoreCase(args[0])) {
            reply("Usage: .config save");
            return;
        }
        ConfigManager.saveNow();
        reply("Saved config to " + ConfigManager.getConfigDir());
    }

    private static void exportProfile() {
        String name = ConfigManager.getCurrentProfile();
        if (ConfigManager.exportProfile(name)) {
            reply("Exported profile to " + ConfigManager.getProfilesDir().resolve(name + ".json"));
        } else {
            reply("Export failed");
        }
    }

    private static void importProfile(String name) {
        if (name == null || name.isBlank()) {
            reply("Usage: .profile import <name>");
            return;
        }
        reply(ConfigManager.importProfile(name) ? "Imported " + name : "Import failed");
    }

    private static void openDeveloperScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.setScreen(new DeveloperScreen(mc.currentScreen));
    }

    private static void applySetting(Setting<?> setting, String value) {
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.setValue(Boolean.parseBoolean(value));
        } else if (setting instanceof NumberSetting numberSetting) {
            numberSetting.setValue(Double.parseDouble(value));
        } else if (setting instanceof ColorSetting colorSetting) {
            colorSetting.setValue(parseColor(value, colorSetting.getValue()));
        } else if (setting instanceof KeybindSetting keybindSetting) {
            keybindSetting.setValue(parseKey(value));
        }
    }

    private static Module findModule(String[] args, int start) {
        return findModule(args, start, args.length);
    }

    private static Module findModule(String[] args, int start, int end) {
        String wanted = normalize(join(args, start, end));
        if (wanted.isBlank()) return null;
        for (Module module : ModuleManager.getModules()) {
            if (normalize(module.getName()).equals(wanted)) return module;
        }
        return null;
    }

    private static Setting<?> findSetting(Module module, String name) {
        String wanted = normalize(name);
        for (Setting<?> setting : module.getSettings()) {
            if (normalize(setting.getName()).equals(wanted)) return setting;
        }
        return null;
    }

    private static int parseKey(String keyName) {
        String key = keyName.trim().toUpperCase(Locale.ROOT);
        if (key.equals("NONE") || key.equals("UNKNOWN") || key.equals("UNBIND")) return GLFW.GLFW_KEY_UNKNOWN;
        if (key.length() == 1) return GLFW.glfwGetKeyScancode(key.charAt(0)) >= 0 ? key.charAt(0) : GLFW.GLFW_KEY_UNKNOWN;

        return switch (key) {
            case "RSHIFT", "RIGHT_SHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LSHIFT", "LEFT_SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RCTRL", "RIGHT_CONTROL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LCTRL", "LEFT_CONTROL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            default -> {
                if (key.matches("F\\d+")) {
                    int fn = Integer.parseInt(key.substring(1));
                    yield fn >= 1 && fn <= 25 ? GLFW.GLFW_KEY_F1 + fn - 1 : GLFW.GLFW_KEY_UNKNOWN;
                }
                yield GLFW.GLFW_KEY_UNKNOWN;
            }
        };
    }

    private static int parseColor(String value, int fallback) {
        String cleaned = value.trim();
        if (cleaned.startsWith("#")) cleaned = cleaned.substring(1);
        if (cleaned.startsWith("0x") || cleaned.startsWith("0X")) cleaned = cleaned.substring(2);
        try {
            return (int) Long.parseLong(cleaned, 16) | 0xFF000000;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String join(String[] args, int start, int end) {
        if (start >= end || start >= args.length) return "";
        return String.join(" ", List.of(args).subList(start, Math.min(end, args.length)));
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "");
    }

    public static void reply(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(Text.literal("[Liquor] " + message));
        } else {
            NotificationManager.push("Liquor", message);
        }
    }
}
