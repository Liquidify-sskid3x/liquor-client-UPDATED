package com.example.liquorclient.accounts;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.gui.NotificationManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class CrackedAccountManager {
    private static final List<CrackedAccount> accounts = new ArrayList<>();
    private static String selectedName = "";

    private CrackedAccountManager() {
    }

    public static void load(JsonArray accountJson, String selected) {
        accounts.clear();
        selectedName = "";

        Path accountsDir = ConfigManager.getConfigDir().resolve("accounts");
        if (Files.exists(accountsDir)) {
            try (Stream<Path> stream = Files.list(accountsDir)) {
                stream.filter(p -> p.toString().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                String content = Files.readString(p, StandardCharsets.UTF_8);
                                JsonElement element = JsonParser.parseString(content);
                                if (element.isJsonObject()) {
                                    JsonObject obj = element.getAsJsonObject();
                                    if (obj.has("name")) {
                                        String name = obj.get("name").getAsString();
                                        if (isValidName(name) && find(name).isEmpty()) {
                                            accounts.add(new CrackedAccount(name));
                                        }
                                    }
                                }
                            } catch (IOException | RuntimeException ignored) {
                            }
                        });
            } catch (IOException ignored) {
            }
        }

        if (accounts.isEmpty()) {
            addInternal("DevPlayer");
        }

        if (isValidName(selected)) {
            Optional<CrackedAccount> found = find(selected);
            if (found.isPresent()) {
                selectedName = found.get().name();
                applySession(found.get());
            } else if (!accounts.isEmpty()) {
                selectedName = accounts.get(0).name();
                applySession(accounts.get(0));
            }
        } else if (!accounts.isEmpty()) {
            selectedName = accounts.get(0).name();
            applySession(accounts.get(0));
        }
    }

    public static JsonArray toJson() {
        JsonArray array = new JsonArray();
        for (CrackedAccount account : accounts) {
            JsonObject object = new JsonObject();
            object.addProperty("name", account.name());
            object.addProperty("uuid", account.uuid().toString());
            array.add(object);
        }
        return array;
    }

    public static List<CrackedAccount> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public static Optional<CrackedAccount> getSelected() {
        return find(selectedName);
    }

    public static String getSelectedName() {
        return selectedName == null ? "" : selectedName;
    }

    public static boolean add(String name) {
        if (!addInternal(name)) return false;
        selectedName = normalizeName(name);

        find(selectedName).ifPresent(CrackedAccountManager::applySession);

        ConfigManager.requestSave();
        NotificationManager.push("Cracked Account", "Added " + selectedName);
        return true;
    }

    public static boolean select(String name) {
        Optional<CrackedAccount> account = find(name);
        if (account.isEmpty()) return false;
        selectedName = account.get().name();

        applySession(account.get());

        ConfigManager.requestSave();
        NotificationManager.push("Cracked Account", "Selected " + selectedName);
        return true;
    }

    public static void applySession(CrackedAccount account) {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc == null) return;

        try {
            ((com.example.liquorclient.mixin.MinecraftClientAccessor) mc).liquor$setSession(
                new net.minecraft.client.session.Session(
                    account.name(),
                    account.uuid(),
                    "",
                    java.util.Optional.empty(),
                    java.util.Optional.empty()
                )
            );
        } catch (Exception ignored) {
        }
    }

    public static boolean delete(String name) {
        if (accounts.size() <= 1) return false;
        String normalized = normalizeName(name);
        boolean removed = accounts.removeIf(account -> account.name().equalsIgnoreCase(normalized));
        if (!removed) return false;

        if (selectedName.equalsIgnoreCase(normalized)) {
            selectedName = accounts.get(0).name();
        }

        ConfigManager.requestSave();
        NotificationManager.push("Cracked Account", "Deleted " + normalized);
        return true;
    }

    public static boolean isValidName(String name) {
        String normalized = normalizeName(name);
        return normalized.length() >= 3 && normalized.length() <= 16 && normalized.matches("[A-Za-z0-9_]+");
    }

    public static String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private static Optional<CrackedAccount> find(String name) {
        String normalized = normalizeName(name);
        return accounts.stream()
                .filter(account -> account.name().equalsIgnoreCase(normalized))
                .findFirst();
    }

    private static boolean addInternal(String name) {
        String normalized = normalizeName(name);
        if (!isValidName(normalized) || find(normalized).isPresent()) return false;
        accounts.add(new CrackedAccount(normalized));
        return true;
    }
}
