package com.example.liquorclient.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public class PacketUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void send(Packet<?> packet) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    public static void sendQuietly(Packet<?> packet) {
        if (mc.getNetworkHandler() != null) {
            // This is just a wrapper for now, could be used for silent sending if we hook network
            mc.getNetworkHandler().sendPacket(packet);
        }
    }
}
