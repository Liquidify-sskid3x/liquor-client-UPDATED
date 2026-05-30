package com.example.liquorclient.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class FriendManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FRIENDS_FILE = FabricLoader.getInstance().getGameDir().resolve("liquor").resolve("friends.json");
    private static final Set<String> friends = new HashSet<>();
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void load() {
        try {
            if (Files.exists(FRIENDS_FILE)) {
                String content = Files.readString(FRIENDS_FILE, StandardCharsets.UTF_8);
                Set<String> loaded = GSON.fromJson(content, new TypeToken<Set<String>>() {}.getType());
                if (loaded != null) {
                    friends.clear();
                    friends.addAll(loaded);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FRIENDS_FILE.getParent());
            Files.writeString(FRIENDS_FILE, GSON.toJson(friends), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFriend(String name) {
        return friends.contains(name);
    }

    public static boolean isFriend(PlayerEntity player) {
        return player != null && player != mc.player && friends.contains(player.getName().getString());
    }

    public static void addFriend(String name) {
        friends.add(name);
        save();
    }

    public static void removeFriend(String name) {
        friends.remove(name);
        save();
    }

    public static Set<String> getFriends() {
        return new HashSet<>(friends);
    }

    public static void clear() {
        friends.clear();
        save();
    }
}
