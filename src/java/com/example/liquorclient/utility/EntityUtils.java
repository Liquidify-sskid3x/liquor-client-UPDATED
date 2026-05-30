package com.example.liquorclient.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static List<Entity> getEntities() {
        if (mc.world == null) return new ArrayList<>();
        return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .collect(Collectors.toList());
    }

    public static List<PlayerEntity> getPlayers() {
        if (mc.world == null) return new ArrayList<>();
        return new ArrayList<>(mc.world.getPlayers());
    }

    public static List<LivingEntity> getLivingEntities() {
        return getEntities().stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());
    }

    public static Entity getClosestEntity(double range) {
        if (mc.player == null) return null;
        Entity closest = null;
        double dist = range;

        for (Entity e : getEntities()) {
            if (e == mc.player) continue;
            double d = mc.player.distanceTo(e);
            if (d < dist) {
                dist = d;
                closest = e;
            }
        }
        return closest;
    }

    public static double getDistanceSq(Entity e1, Entity e2) {
        return e1.squaredDistanceTo(e2);
    }
}
