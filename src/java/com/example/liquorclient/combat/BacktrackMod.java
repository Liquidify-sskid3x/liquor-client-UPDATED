package com.example.liquorclient.combat;

import com.example.liquorclient.mixin.EntityS2CPacketAccessor;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.Setting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPosition;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class BacktrackMod extends Module {
    private static final double PACKET_DELTA_SCALE = 4096.0;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final NumberSetting range = new NumberSetting("Range", 3.0, 1.0, 6.0);
    private final NumberSetting delay = new NumberSetting("Delay", 90.0, 25.0, 400.0);
    private final BooleanSetting smartDelay = new BooleanSetting("Smart Delay", true);
    private final BooleanSetting backtrackDelta = new BooleanSetting("Backtrack Delta", true);
    private final NumberSetting smartClose = new NumberSetting("Smart Close", 2.75, 1.5, 4.5);
    private final BooleanSetting stealth = new BooleanSetting("Stealth", true);
    private final NumberSetting maxQueue = new NumberSetting("Max Queue", 6.0, 2.0, 16.0);
    private final NumberSetting releasePerTick = new NumberSetting("Release/Tick", 1.0, 1.0, 3.0);

    private final Deque<PacketSnapshot> packetQueue = new ArrayDeque<>();
    private volatile int targetEntityId = -1;
    private volatile boolean shouldBacktrackFlag = false;
    private boolean draining = false;
    private long lastReleaseMs = 0L;
    private Vec3d trackedRealPos = null;
    private double lastClientDistance = Double.MAX_VALUE;
    private int lastTrackedTargetId = -1;

    public BacktrackMod() {
        super("Backtrack", Category.COMBAT, "Rewinds past positions for better hit registration");
        addSetting(range);
        addSetting(delay);
        addSetting(smartDelay);
        addSetting(backtrackDelta);
        addSetting(smartClose);
        addSetting(stealth);
        addSetting(maxQueue);
        addSetting(releasePerTick);
    }

    @com.example.liquorclient.event.Subscribe
    private void onTick(com.example.liquorclient.event.impl.TickEvent.Post event) {
        tick();
    }

    @com.example.liquorclient.event.Subscribe
    private void onReceive(com.example.liquorclient.event.impl.PacketEvent.Receive event) {
        if (onReceivePacket(event.getPacket())) {
            event.cancel();
        }
    }

    private void tick() {
        updateSettingVisibility();

        if (!isEnabled() || mc.world == null || mc.player == null) {
            resetState(true);
            return;
        }

        updateTarget();
        syncTrackedPosition();

        boolean shouldHold = calculateShouldBacktrack();
        if (shouldHold) {
            draining = false;
            shouldBacktrackFlag = true;
        } else {
            shouldBacktrackFlag = false;
            if (!packetQueue.isEmpty()) {
                draining = true;
            }
        }

        if (draining || shouldHold) {
            releasePackets(false);
        }

        trimQueueHardCap();
        updateDistanceMemory();
    }

    private void updateSettingVisibility() {
        boolean smart = smartDelay.getValue();
        backtrackDelta.setVisible(smart);
        smartClose.setVisible(smart);
    }

    @Override
    public List<Setting<?>> getSettings() {
        updateSettingVisibility();
        return super.getSettings();
    }

    private void updateTarget() {
        if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity().isAlive()) {
            int id = ehr.getEntity().getId();
            if (id != lastTrackedTargetId) {
                resetTracking(ehr.getEntity());
            }
            targetEntityId = id;
            return;
        }

        if (targetEntityId != -1) {
            Entity tracked = mc.world.getEntityById(targetEntityId);
            if (tracked == null || !tracked.isAlive()) {
                clearTarget();
                return;
            }
            double maxRange = range.getValue();
            if (mc.player.squaredDistanceTo(tracked) > maxRange * maxRange * 1.5) {
                clearTarget();
            }
        }
    }

    private void resetTracking(Entity entity) {
        lastTrackedTargetId = entity.getId();
        trackedRealPos = entityPosition(entity);
        lastClientDistance = mc.player.distanceTo(entity);
    }

    private void clearTarget() {
        targetEntityId = -1;
        lastTrackedTargetId = -1;
        trackedRealPos = null;
        lastClientDistance = Double.MAX_VALUE;
    }

    private void syncTrackedPosition() {
        Entity tracked = getTrackedEntity();
        if (tracked == null) return;

        if (trackedRealPos == null) {
            trackedRealPos = entityPosition(tracked);
            return;
        }

        if (packetQueue.isEmpty() && !shouldBacktrackFlag) {
            trackedRealPos = entityPosition(tracked);
        }
    }

    private void updateDistanceMemory() {
        Entity tracked = getTrackedEntity();
        if (tracked == null) return;
        lastClientDistance = mc.player.distanceTo(tracked);
    }

    private boolean calculateShouldBacktrack() {
        if (targetEntityId == -1) return false;
        Entity tracked = getTrackedEntity();
        if (tracked == null || !tracked.isAlive()) return false;

        double maxRange = range.getValue();
        double clientDist = mc.player.distanceTo(tracked);

        // Clack: "hold it if its further then last position"
        if (backtrackDelta.getValue() && clientDist > lastClientDistance + 0.003 && clientDist <= maxRange * 1.5) {
            return true;
        }

        if (clientDist <= maxRange) return true;

        if (smartDelay.getValue() && trackedRealPos != null) {
            double realDist = entityPosition(mc.player).distanceTo(trackedRealPos);
            if (realDist <= maxRange && clientDist <= maxRange * 1.15) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldSmartPause(Entity tracked) {
        if (!smartDelay.getValue() || tracked == null) return false;

        double clientDist = mc.player.distanceTo(tracked);

        if (backtrackDelta.getValue()) {
            // Clack: "release queues once hes close"
            if (clientDist <= smartClose.getValue()) return false;

            // Clack: "hold it if its further then last position"
            if (clientDist > lastClientDistance + 0.003) return true;
        }

        if (trackedRealPos == null) return false;
        double realDist = entityPosition(mc.player).distanceTo(trackedRealPos);
        double closeRange = smartClose.getValue();

        if (clientDist <= closeRange) return true;

        boolean approaching = clientDist < lastClientDistance - 0.04;
        if (approaching && clientDist <= range.getValue() * 0.9) return true;

        return realDist + 0.12 < clientDist && clientDist <= range.getValue();
    }

    private boolean shouldSmartRelease(Entity tracked) {
        if (!smartDelay.getValue() || tracked == null) return false;

        double clientDist = mc.player.distanceTo(tracked);

        if (backtrackDelta.getValue()) {
            // Clack: "release queues once hes close"
            if (clientDist <= smartClose.getValue()) return true;

            // If he's moving towards us, release
            if (clientDist < lastClientDistance - 0.003) return true;
        }

        double maxRange = range.getValue();

        if (clientDist > maxRange * 0.7) return true;
        if (clientDist > lastClientDistance + 0.06) return true;

        if (trackedRealPos != null) {
            double realDist = entityPosition(mc.player).distanceTo(trackedRealPos);
            if (realDist > clientDist + 0.25) return true;
        }

        return false;
    }

    public void onSuccessfulAttack() {
        if (!isEnabled() || targetEntityId == -1) return;
        if (stealth.getValue() || smartDelay.getValue()) {
            releasePackets(true);
        }
    }

    private void releasePackets(boolean burstOnHit) {
        if (packetQueue.isEmpty()) {
            draining = false;
            return;
        }

        Entity tracked = getTrackedEntity();
        if (!burstOnHit && shouldSmartPause(tracked)) {
            return;
        }

        long now = System.currentTimeMillis();
        int budget = burstOnHit ? 2 : (int) Math.round(releasePerTick.getValue());
        if (!stealth.getValue() && !smartDelay.getValue()) {
            budget = Integer.MAX_VALUE;
        } else if (shouldSmartRelease(tracked)) {
            budget = Math.max(budget, 3);
        }

        int released = 0;
        while (!packetQueue.isEmpty() && released < budget) {
            PacketSnapshot head = packetQueue.peekFirst();
            if (head == null) break;

            long age = now - head.timestamp;
            long requiredDelay = burstOnHit ? 0L : effectiveDelayMs(tracked);

            if (!burstOnHit && age < requiredDelay) {
                break;
            }

            if (stealth.getValue() && !burstOnHit && now - lastReleaseMs < 45L) {
                break;
            }

            packetQueue.pollFirst();
            handlePacket(head.packet);
            lastReleaseMs = now;
            released++;
        }

        if (packetQueue.isEmpty()) {
            draining = false;
        }
    }

    private void trimQueueHardCap() {
        int cap = (int) Math.round(maxQueue.getValue());
        Entity tracked = getTrackedEntity();
        if (smartDelay.getValue() && shouldSmartPause(tracked)) {
            cap = Math.min(cap + 2, (int) Math.round(maxQueue.getValue()) + 4);
        }

        while (packetQueue.size() > cap) {
            handlePacket(packetQueue.pollFirst().packet);
            lastReleaseMs = System.currentTimeMillis();
        }
    }

    private long effectiveDelayMs(Entity tracked) {
        long base = delay.getValue().longValue();

        if (tracked != null && smartDelay.getValue()) {
            if (shouldSmartPause(tracked)) {
                return Math.max(60L, Math.round(base * 2.4));
            }
            if (shouldSmartRelease(tracked)) {
                return Math.max(25L, Math.round(base * 0.45));
            }
        }

        if (!stealth.getValue() || tracked == null) {
            return base;
        }

        double maxRange = Math.max(0.5, range.getValue());
        double dist = mc.player.distanceTo(tracked);
        double proximity = 1.0 - Math.min(1.0, dist / maxRange);
        return Math.max(35L, Math.round(base * (0.55 + proximity * 0.45)));
    }

    private Entity getTrackedEntity() {
        if (targetEntityId == -1 || mc.world == null) return null;
        return mc.world.getEntityById(targetEntityId);
    }

    private static Vec3d entityPosition(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }

    private void advanceTrackedRealPosition(Packet<?> packet, Entity tracked) {
        if (trackedRealPos == null) {
            trackedRealPos = entityPosition(tracked);
        }

        if (packet instanceof EntityPositionS2CPacket posPacket) {
            EntityPosition current = new EntityPosition(
                    trackedRealPos,
                    tracked.getVelocity(),
                    tracked.getYaw(),
                    tracked.getPitch()
            );
            EntityPosition next = EntityPosition.apply(current, posPacket.change(), posPacket.relatives());
            trackedRealPos = next.position();
        } else if (packet instanceof EntityS2CPacket move && move.isPositionChanged()) {
            trackedRealPos = trackedRealPos.add(
                    move.getDeltaX() / PACKET_DELTA_SCALE,
                    move.getDeltaY() / PACKET_DELTA_SCALE,
                    move.getDeltaZ() / PACKET_DELTA_SCALE
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePacket(Packet<?> packet) {
        if (!mc.isOnThread()) {
            mc.execute(() -> handlePacket(packet));
            return;
        }
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler != null) {
            ((Packet<ClientPlayPacketListener>) packet).apply(handler);
        }
    }

    public boolean onReceivePacket(Packet<?> packet) {
        if (!isEnabled()) return false;

        if (packet instanceof GameJoinS2CPacket
                || packet instanceof DisconnectS2CPacket
                || packet instanceof PlayerPositionLookS2CPacket) {
            return false;
        }

        int entityId = packetEntityId(packet);
        if (entityId == -1 || entityId != targetEntityId) return false;
        if (mc.player != null && entityId == mc.player.getId()) return false;

        if (packet instanceof EntityS2CPacket || packet instanceof EntityPositionS2CPacket) {
            Entity tracked = getTrackedEntity();
            if (tracked != null) {
                advanceTrackedRealPosition(packet, tracked);
            }

            if (shouldBacktrackFlag) {
                packetQueue.addLast(new PacketSnapshot(packet, System.currentTimeMillis()));
                return true;
            }
        }

        return false;
    }

    private static int packetEntityId(Packet<?> packet) {
        if (packet instanceof EntityPositionS2CPacket position) {
            return position.entityId();
        }
        if (packet instanceof EntityS2CPacket entityPacket) {
            return ((EntityS2CPacketAccessor) entityPacket).liquor$getEntityId();
        }
        return -1;
    }

    private void resetState(boolean flushNow) {
        shouldBacktrackFlag = false;
        clearTarget();
        draining = false;

        if (flushNow) {
            flushRemaining(true);
        } else {
            packetQueue.clear();
        }
    }

    private void flushRemaining(boolean immediate) {
        if (immediate || (!stealth.getValue() && !smartDelay.getValue())) {
            while (!packetQueue.isEmpty()) {
                handlePacket(packetQueue.pollFirst().packet);
            }
            draining = false;
            return;
        }

        draining = true;
        releasePackets(false);
    }

    @Override
    protected void onDisable() {
        resetState(true);
    }

    private static final class PacketSnapshot {
        final Packet<?> packet;
        final long timestamp;

        PacketSnapshot(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }
}
