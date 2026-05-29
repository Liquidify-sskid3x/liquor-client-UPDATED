package com.example.liquorclient.combat;

import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

public class TickBaseMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean skipTickFlag = false;
    private static int ticksToSkip = 0;

    private final BooleanSetting pastMode = new BooleanSetting("Past Mode", true);
    private final NumberSetting maxTicks = new NumberSetting("Max Ticks", 10.0, 1.0, 30.0);
    private final NumberSetting range = new NumberSetting("Range", 3.5, 0.0, 6.0);
    private final BooleanSetting onlyOnTarget = new BooleanSetting("Only On Target", true);
    private final NumberSetting changeChance = new NumberSetting("Change %", 100.0, 0.0, 100.0);
    private final NumberSetting pauseAfter = new NumberSetting("Pause After", 0.0, 0.0, 10.0);

    private final Deque<TickData> tickHistory = new ArrayDeque<>();
    private int pauseTicks = 0;

    public TickBaseMod() {
        super("TickBase", Category.COMBAT, "Skip game ticks for past/future positional advantage");
        addSetting(pastMode);
        addSetting(maxTicks);
        addSetting(range);
        addSetting(onlyOnTarget);
        addSetting(changeChance);
        addSetting(pauseAfter);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled() || mc.player == null || mc.world == null) {
                tickHistory.clear();
                skipTickFlag = false;
                ticksToSkip = 0;
                pauseTicks = 0;
                return;
            }

            if (pauseTicks > 0) {
                pauseTicks--;
                return;
            }

            Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            tickHistory.addLast(new TickData(playerPos, mc.player.getVelocity(), (float) mc.player.fallDistance, mc.player.isOnGround(), mc.player.horizontalCollision));

            int maxHist = (int) (double) maxTicks.getValue() * 2;
            while (tickHistory.size() > maxHist) {
                tickHistory.pollFirst();
            }

            Entity target = getTarget();
            if (target == null) {
                skipTickFlag = false;
                ticksToSkip = 0;
                return;
            }

            if (Math.random() * 100 > changeChance.getValue()) return;

            if (pastMode.getValue()) {
                handlePastMode(target);
            } else {
                handleFutureMode(target);
            }
        });
    }

    private void handlePastMode(Entity target) {
        if (tickHistory.size() < 3) return;

        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        double currentDist = distance(mc.player.getX(), mc.player.getY(), mc.player.getZ(), targetPos);
        int maxT = (int) (double) maxTicks.getValue();
        int bestIndex = -1;
        double bestDist = currentDist;

        int start = Math.max(0, tickHistory.size() - maxT - 1);
        int idx = 0;
        for (TickData data : tickHistory) {
            if (idx < start) { idx++; continue; }
            double dist = distance(data.pos, targetPos);
            if (dist < bestDist) {
                bestDist = dist;
                bestIndex = idx;
            }
            idx++;
        }

        if (bestIndex >= 0 && bestDist < currentDist - 0.05) {
            int skipCount = tickHistory.size() - bestIndex - 1;
            if (skipCount > 0) {
                scheduleSkip(skipCount);
            }
        }
    }

    private void handleFutureMode(Entity target) {
        if (mc.player.isOnGround() && mc.player.input.getMovementInput().x == 0f && mc.player.input.getMovementInput().y == 0f) return;

        SimulatedPlayer sim = new SimulatedPlayer(mc.player);
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        int maxT = (int) (double) maxTicks.getValue();
        double currentDist = distance(mc.player.getX(), mc.player.getY(), mc.player.getZ(), targetPos);

        int bestTick = -1;
        double bestDist = currentDist;

        for (int i = 1; i <= maxT; i++) {
            sim.simulateTick();
            double dist = sim.distanceTo(targetPos);
            if (dist < bestDist) {
                bestDist = dist;
                bestTick = i;
            }
        }

        if (bestTick > 0 && bestDist < currentDist - 0.05) {
            scheduleSkip(bestTick);
        }
    }

    private static double distance(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double distance(double x1, double y1, double z1, Vec3d b) {
        double dx = x1 - b.x;
        double dy = y1 - b.y;
        double dz = z1 - b.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void scheduleSkip(int count) {
        int pause = (int) (double) pauseAfter.getValue();
        ticksToSkip = count;
        pauseTicks = count + pause;
        skipTickFlag = true;
    }

    private Entity getTarget() {
        if (!onlyOnTarget.getValue()) {
            return mc.world.getEntitiesByClass(LivingEntity.class, mc.player.getBoundingBox().expand(range.getValue()), e ->
                    e != mc.player && e.isAlive() && mc.player.distanceTo(e) <= range.getValue()
            ).stream().findFirst().orElse(null);
        }
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) mc.crosshairTarget).getEntity();
        }
        return null;
    }

    public static boolean shouldSkipTick() {
        if (ticksToSkip > 0) {
            ticksToSkip--;
            if (ticksToSkip <= 0) skipTickFlag = false;
            return true;
        }
        return false;
    }

    @Override
    protected void onDisable() {
        tickHistory.clear();
        skipTickFlag = false;
        ticksToSkip = 0;
        pauseTicks = 0;
    }

    private record TickData(Vec3d pos, Vec3d velocity, float fallDistance, boolean onGround, boolean collidedHorizontally) {}
}
