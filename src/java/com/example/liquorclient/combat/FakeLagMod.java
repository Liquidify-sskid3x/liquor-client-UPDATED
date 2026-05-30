package com.example.liquorclient.combat;

import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

public class FakeLagMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final NumberSetting delayMs = new NumberSetting("Delay", 300.0, 0.0, 1000.0);
    private final NumberSetting maxQueue = new NumberSetting("Max Queue", 10.0, 1.0, 50.0);
    private final BooleanSetting pauseOnNoMove = new BooleanSetting("Pause On No Move", true);
    private final BooleanSetting flushOnAttack = new BooleanSetting("Flush On Attack", true);

    private final Deque<PacketSnapshot> packetQueue = new ArrayDeque<>();
    private boolean flushing = false;

    public FakeLagMod() {
        super("Fake Lag", Category.COMBAT, "Delays outgoing movement packets to create a lag effect");
        addSetting(delayMs);
        addSetting(maxQueue);
        addSetting(pauseOnNoMove);
        addSetting(flushOnAttack);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled() || mc.player == null || mc.world == null) {
                flushAll();
                return;
            }
            if (!flushing) {
                processPackets();
            }
            flushing = false;
        });
    }

    public boolean onSendPacket(Packet<?> packet) {
        if (!isEnabled() || mc.player == null) return false;
        if (flushing) return false;

        if (pauseOnNoMove.getValue()) {
            boolean isMoving = mc.player.input.getMovementInput().x != 0f || mc.player.input.getMovementInput().y != 0f;
            if (!isMoving) return false;
        }

        if (packet instanceof PlayerMoveC2SPacket) {
            if (packetQueue.size() >= maxQueue.getValue()) {
                PacketSnapshot oldest = packetQueue.pollFirst();
                if (oldest != null) {
                    sendDirect(oldest.packet);
                }
            }
            packetQueue.addLast(new PacketSnapshot(packet, System.currentTimeMillis()));
            return true;
        }

        if (flushOnAttack.getValue() && (packet instanceof PlayerInteractEntityC2SPacket || packet instanceof HandSwingC2SPacket)) {
            flushAll();
            return false;
        }

        return false;
    }

    private void processPackets() {
        if (packetQueue.isEmpty()) return;
        long now = System.currentTimeMillis();
        int released = 0;
        flushing = true;
        while (!packetQueue.isEmpty() && released < 3) {
            PacketSnapshot head = packetQueue.peekFirst();
            if (now - head.timestamp < delayMs.getValue().longValue()) break;
            packetQueue.pollFirst();
            sendDirect(head.packet);
            released++;
        }
        flushing = false;
    }

    public void flushAll() {
        flushing = true;
        while (!packetQueue.isEmpty()) {
            sendDirect(packetQueue.pollFirst().packet);
        }
    }

    private void sendDirect(Packet<?> packet) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    @Override
    protected void onDisable() {
        flushAll();
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
