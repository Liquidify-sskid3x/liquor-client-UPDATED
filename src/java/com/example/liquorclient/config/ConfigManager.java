package com.example.liquorclient.config;

import com.example.liquorclient.accounts.CrackedAccount;
import com.example.liquorclient.accounts.CrackedAccountManager;
import com.example.liquorclient.gui.ArrayListHud;
import com.example.liquorclient.gui.NotificationManager;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.ColorSetting;
import com.example.liquorclient.module.KeybindSetting;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getGameDir().resolve("liquor");
    private static final Path PROFILES_DIR = CONFIG_DIR.resolve("profiles");
    private static final Path ACCOUNTS_DIR = CONFIG_DIR.resolve("accounts");
    private static final Path PROFILES_FILE = CONFIG_DIR.resolve("profiles.json");
    private static final Path KEYBINDS_FILE = CONFIG_DIR.resolve("keybinds.json");
    private static final Path INDEX_FILE = CONFIG_DIR.resolve("index.json");
    private static final long SAVE_DELAY_MS = 450L;

    private static String currentProfile = "default";
    private static boolean initialized = false;
    private static boolean applying = false;
    private static boolean dirty = false;
    private static long lastDirtyTime = 0L;

    private ConfigManager() {
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        ensureDirectories();
        loadIndex();
        loadCurrentProfile();
        loadKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> flushIfReady());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveNow());
    }

    public static void requestSave() {
        if (!initialized || applying) return;
        dirty = true;
        lastDirtyTime = System.currentTimeMillis();
    }

    public static void saveNow() {
        if (!initialized || applying) return;

        ensureDirectories();
        saveCurrentProfile();
        saveKeybinds();
        saveIndex();
        dirty = false;
    }

    public static String getCurrentProfile() {
        return currentProfile;
    }

    public static Path getProfilesDir() {
        return PROFILES_DIR;
    }

    public static List<String> getProfiles() {
        ensureDirectories();
        List<String> profiles = new ArrayList<>();

        JsonObject root = readObject(PROFILES_FILE);
        if (root != null && root.has("profiles") && root.get("profiles").isJsonObject()) {
            for (String name : root.getAsJsonObject("profiles").keySet()) {
                profiles.add(name);
            }
        }

        if (!profiles.contains(currentProfile)) {
            profiles.add(currentProfile);
        }
        if (!profiles.contains("default")) {
            profiles.add("default");
        }

        Collections.sort(profiles);
        return profiles;
    }

    public static List<String> getExportedProfiles() {
        ensureDirectories();
        List<String> files = new ArrayList<>();
        if (!Files.exists(PROFILES_DIR)) return files;
        try (Stream<Path> stream = Files.list(PROFILES_DIR)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .forEach(p -> {
                        String name = p.getFileName().toString();
                        files.add(name.substring(0, name.length() - 5));
                    });
        } catch (IOException ignored) {
        }
        return files;
    }

    public static boolean createProfile(String name) {
        String sanitized = sanitizeProfileName(name);
        if (sanitized.isBlank()) return false;

        saveNow();
        currentProfile = sanitized;
        saveCurrentProfile();
        saveIndex();
        dirty = false;
        NotificationManager.push("Profile", "Created " + sanitized);
        return true;
    }

    public static boolean exportProfile(String name) {
        String sanitized = sanitizeProfileName(name);
        if (sanitized.isBlank()) return false;

        saveNow();
        JsonObject profile = readProfile(currentProfile);
        if (profile == null) return false;

        Path exportFile = PROFILES_DIR.resolve(sanitized + ".json");
        writeObject(exportFile, profile);
        NotificationManager.push("Profile", "Exported as " + sanitized);
        return true;
    }

    public static boolean importProfile(String fileName) {
        if (fileName == null || fileName.isBlank()) return false;

        String cleanName = fileName.trim();
        if (cleanName.endsWith(".json")) cleanName = cleanName.substring(0, cleanName.length() - 5);
        String sanitized = sanitizeProfileName(cleanName);
        if (sanitized.isBlank()) return false;

        Path importFile = PROFILES_DIR.resolve(sanitized + ".json");
        if (!Files.exists(importFile)) {
            importFile = PROFILES_DIR.resolve(fileName.trim());
            if (!Files.exists(importFile)) return false;
        }

        JsonObject imported = readObject(importFile);
        if (imported == null || !imported.has("modules")) return false;

        JsonObject root = readProfilesRoot();
        root.addProperty("currentProfile", sanitized);
        root.getAsJsonObject("profiles").add(sanitized, imported);
        writeObject(PROFILES_FILE, root);
        currentProfile = sanitized;
        loadCurrentProfile();
        saveIndex();
        dirty = false;
        NotificationManager.push("Profile", "Imported " + sanitized);
        return true;
    }

    public static boolean renameProfile(String oldName, String newName) {
        String sanitizedOld = sanitizeProfileName(oldName);
        String sanitizedNew = sanitizeProfileName(newName);
        if (sanitizedOld.isBlank() || sanitizedNew.isBlank()) return false;
        if (sanitizedOld.equals(sanitizedNew)) return true;
        if ("default".equals(sanitizedOld) || "default".equals(sanitizedNew)) return false;

        JsonObject root = readProfilesRoot();
        JsonObject profiles = root.getAsJsonObject("profiles");
        if (!profiles.has(sanitizedOld)) return false;
        if (profiles.has(sanitizedNew)) return false;

        profiles.add(sanitizedNew, profiles.remove(sanitizedOld));

        if (sanitizedOld.equals(currentProfile)) {
            currentProfile = sanitizedNew;
        }

        root.addProperty("currentProfile", currentProfile);
        writeObject(PROFILES_FILE, root);
        saveIndex();
        dirty = false;
        NotificationManager.push("Profile", "Renamed to " + sanitizedNew);
        return true;
    }

    public static boolean switchProfile(String name) {
        String sanitized = sanitizeProfileName(name);
        if (sanitized.isBlank()) return false;
        if (sanitized.equals(currentProfile)) return true;

        saveNow();
        currentProfile = sanitized;
        loadCurrentProfile();
        saveProfileSelection();
        saveIndex();
        dirty = false;
        NotificationManager.push("Profile", "Loaded " + sanitized);
        return true;
    }

    public static boolean deleteProfile(String name) {
        String sanitized = sanitizeProfileName(name);
        if (sanitized.isBlank() || "default".equals(sanitized)) return false;

        JsonObject root = readProfilesRoot();
        root.getAsJsonObject("profiles").remove(sanitized);

        if (sanitized.equals(currentProfile)) {
            currentProfile = "default";
            loadCurrentProfile();
        }

        root.addProperty("currentProfile", currentProfile);
        writeObject(PROFILES_FILE, root);
        saveIndex();
        dirty = false;
        NotificationManager.push("Profile", "Deleted " + sanitized);
        return true;
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }

    private static void flushIfReady() {
        if (!dirty) return;
        if (System.currentTimeMillis() - lastDirtyTime < SAVE_DELAY_MS) return;
        saveNow();
    }

    private static void loadIndex() {
        JsonObject index = readObject(INDEX_FILE);
        if (index == null) {
            CrackedAccountManager.load(new JsonArray(), "");
        } else {
            JsonArray accounts = index.has("crackedAccounts") && index.get("crackedAccounts").isJsonArray()
                    ? index.getAsJsonArray("crackedAccounts")
                    : new JsonArray();
            String selectedAccount = index.has("selectedCrackedAccount") ? index.get("selectedCrackedAccount").getAsString() : "";
            CrackedAccountManager.load(accounts, selectedAccount);
        }

        JsonObject profilesRoot = readObject(PROFILES_FILE);
        if (profilesRoot != null && profilesRoot.has("currentProfile")) {
            currentProfile = sanitizeProfileName(profilesRoot.get("currentProfile").getAsString());
            if (currentProfile.isBlank()) currentProfile = "default";
        }
    }

    private static void saveIndex() {
        JsonObject index = new JsonObject();
        index.add("crackedAccounts", new JsonArray());
        index.addProperty("selectedCrackedAccount", CrackedAccountManager.getSelectedName());
        writeObject(INDEX_FILE, index);

        for (CrackedAccount account : CrackedAccountManager.getAccounts()) {
            saveAccountFile(account);
        }

        cleanupAccountFiles();
    }

    private static void saveAccountFile(CrackedAccount account) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", account.name());
        obj.addProperty("uuid", account.uuid().toString());
        writeObject(ACCOUNTS_DIR.resolve(account.name() + ".json"), obj);
    }

    private static void cleanupAccountFiles() {
        if (!Files.exists(ACCOUNTS_DIR)) return;
        try (Stream<Path> stream = Files.list(ACCOUNTS_DIR)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        String accountName = fileName.substring(0, fileName.length() - 5);
                        boolean exists = CrackedAccountManager.getAccounts().stream()
                                .anyMatch(a -> a.name().equals(accountName));
                        if (!exists) {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private static void loadCurrentProfile() {
        applying = true;
        try {
            JsonObject root = readProfile(currentProfile);
            if (root == null) return;

            if (root.has("hud") && root.get("hud").isJsonObject()) {
                JsonObject hud = root.getAsJsonObject("hud");
                ArrayListHud.setInfoPositionSilently(getInt(hud, "infoX", 8), getInt(hud, "infoY", 8));
                ArrayListHud.setArrayPositionSilently(getInt(hud, "arrayX", -1), getInt(hud, "arrayY", 8));
                ArrayListHud.setInfoAnchorSilently(getInt(hud, "infoAnchor", 0));
                ArrayListHud.setArrayAnchorSilently(getInt(hud, "arrayAnchor", 0));
                ArrayListHud.setArmorAnchorSilently(getInt(hud, "armorAnchor", 0));
                ArrayListHud.setPotionAnchorSilently(getInt(hud, "potionAnchor", 0));
                ArrayListHud.setKeystrokesAnchorSilently(getInt(hud, "keystrokesAnchor", 0));
                ArrayListHud.setCpsAnchorSilently(getInt(hud, "cpsAnchor", 0));
            }

            if (!root.has("modules") || !root.get("modules").isJsonObject()) return;
            JsonObject modules = root.getAsJsonObject("modules");

            for (Module module : ModuleManager.getModules()) {
                if (!modules.has(module.getName()) || !modules.get(module.getName()).isJsonObject()) continue;
                JsonObject moduleJson = modules.getAsJsonObject(module.getName());

                if (moduleJson.has("settings") && moduleJson.get("settings").isJsonObject()) {
                    JsonObject settings = moduleJson.getAsJsonObject("settings");
                    for (Setting<?> setting : module.getSettings()) {
                        if (setting instanceof KeybindSetting) continue;
                        applySetting(setting, settings.get(setting.getName()));
                    }
                }

                if (moduleJson.has("enabled")) {
                    module.setEnabledSilently(moduleJson.get("enabled").getAsBoolean());
                }
            }
        } finally {
            applying = false;
        }
    }

    private static void saveCurrentProfile() {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();

        for (Module module : ModuleManager.getModules()) {
            JsonObject moduleJson = new JsonObject();
            JsonObject settings = new JsonObject();

            moduleJson.addProperty("enabled", module.isEnabled());
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof KeybindSetting) continue;
                Object value = setting.getValue();
                if (value instanceof Boolean bool) {
                    settings.addProperty(setting.getName(), bool);
                } else if (value instanceof Number number) {
                    settings.addProperty(setting.getName(), number);
                } else if (value instanceof String string) {
                    settings.addProperty(setting.getName(), string);
                }
            }

            moduleJson.add("settings", settings);
            modules.add(module.getName(), moduleJson);
        }

        JsonObject hud = new JsonObject();
        hud.addProperty("infoX", ArrayListHud.getInfoX());
        hud.addProperty("infoY", ArrayListHud.getInfoY());
        hud.addProperty("arrayX", ArrayListHud.getArrayX());
        hud.addProperty("arrayY", ArrayListHud.getArrayY());
        hud.addProperty("armorX", ArrayListHud.getArmorX());
        hud.addProperty("armorY", ArrayListHud.getArmorY());
        hud.addProperty("potionX", ArrayListHud.getPotionX());
        hud.addProperty("potionY", ArrayListHud.getPotionY());
        hud.addProperty("keystrokesX", ArrayListHud.getKeystrokesX());
        hud.addProperty("keystrokesY", ArrayListHud.getKeystrokesY());
        hud.addProperty("cpsX", ArrayListHud.getCpsX());
        hud.addProperty("cpsY", ArrayListHud.getCpsY());
        hud.addProperty("infoAnchor", ArrayListHud.getInfoAnchor());
        hud.addProperty("arrayAnchor", ArrayListHud.getArrayAnchor());
        hud.addProperty("armorAnchor", ArrayListHud.getArmorAnchor());
        hud.addProperty("potionAnchor", ArrayListHud.getPotionAnchor());
        hud.addProperty("keystrokesAnchor", ArrayListHud.getKeystrokesAnchor());
        hud.addProperty("cpsAnchor", ArrayListHud.getCpsAnchor());

        root.addProperty("version", 1);
        root.add("modules", modules);
        root.add("hud", hud);
        JsonObject profilesRoot = readProfilesRoot();
        profilesRoot.addProperty("currentProfile", currentProfile);
        profilesRoot.getAsJsonObject("profiles").add(currentProfile, root);
        writeObject(PROFILES_FILE, profilesRoot);
    }

    private static void saveProfileSelection() {
        JsonObject profilesRoot = readProfilesRoot();
        profilesRoot.addProperty("currentProfile", currentProfile);
        writeObject(PROFILES_FILE, profilesRoot);
    }

    private static void loadKeybinds() {
        applying = true;
        try {
            JsonObject root = readObject(KEYBINDS_FILE);
            if (root == null || !root.has("modules") || !root.get("modules").isJsonObject()) return;
            JsonObject modules = root.getAsJsonObject("modules");
            for (Module module : ModuleManager.getModules()) {
                if (modules.has(module.getName())) {
                    applySetting(module.getKeybind(), modules.get(module.getName()));
                }
            }
        } finally {
            applying = false;
        }
    }

    private static void saveKeybinds() {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();
        for (Module module : ModuleManager.getModules()) {
            modules.addProperty(module.getName(), module.getKeybind().getValue());
        }
        root.addProperty("version", 1);
        root.add("modules", modules);
        writeObject(KEYBINDS_FILE, root);
    }

    private static void applySetting(Setting<?> setting, JsonElement value) {
        if (value == null || value.isJsonNull()) return;

        try {
            if (setting instanceof BooleanSetting booleanSetting) {
                booleanSetting.setValue(value.getAsBoolean());
            } else if (setting instanceof NumberSetting numberSetting) {
                double next = value.getAsDouble();
                next = Math.max(numberSetting.getMin(), Math.min(numberSetting.getMax(), next));
                numberSetting.setValue(next);
            } else if (setting instanceof ColorSetting colorSetting) {
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    colorSetting.setValue(parseColor(value.getAsString(), colorSetting.getValue()));
                } else {
                    colorSetting.setValue(value.getAsInt());
                }
            } else if (setting instanceof KeybindSetting keybindSetting) {
                keybindSetting.setValue(value.getAsInt());
            }
        } catch (RuntimeException ignored) {
        }
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        try {
            return object.has(key) ? object.get(key).getAsInt() : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static JsonObject readProfilesRoot() {
        JsonObject root = readObject(PROFILES_FILE);
        if (root == null) root = new JsonObject();
        if (!root.has("profiles") || !root.get("profiles").isJsonObject()) {
            root.add("profiles", new JsonObject());
        }
        String selected = root.has("currentProfile") ? sanitizeProfileName(root.get("currentProfile").getAsString()) : currentProfile;
        root.addProperty("currentProfile", selected.isBlank() ? "default" : selected);
        return root;
    }

    private static JsonObject readProfile(String profile) {
        JsonObject root = readProfilesRoot();
        JsonObject profiles = root.getAsJsonObject("profiles");
        String sanitized = sanitizeProfileName(profile);
        if (profiles.has(sanitized) && profiles.get(sanitized).isJsonObject()) {
            return profiles.getAsJsonObject(sanitized);
        }

        Path oldProfile = FabricLoader.getInstance().getConfigDir()
                .resolve("liquorclient")
                .resolve("profiles")
                .resolve(sanitized + ".json");
        return readObject(oldProfile);
    }

    private static int parseColor(String value, int fallback) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.startsWith("#")) cleaned = cleaned.substring(1);
        if (cleaned.startsWith("0x") || cleaned.startsWith("0X")) cleaned = cleaned.substring(2);
        try {
            return (int) Long.parseLong(cleaned, 16) | 0xFF000000;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String sanitizeProfileName(String name) {
        if (name == null) return "";
        String lower = name.trim().toLowerCase(Locale.ROOT);
        String cleaned = lower.replaceAll("[^a-z0-9_-]", "_");
        return cleaned.length() > 32 ? cleaned.substring(0, 32) : cleaned;
    }

    private static JsonObject readObject(Path path) {
        if (!Files.exists(path)) return null;

        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static void writeObject(Path path, JsonObject object) {
        try {
            ensureDirectories();
            Files.writeString(path, GSON.toJson(object), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static void ensureDirectories() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.createDirectories(PROFILES_DIR);
            Files.createDirectories(ACCOUNTS_DIR);
        } catch (IOException ignored) {
        }
    }
}
