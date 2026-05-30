package com.example.liquorclient.utility;

import com.example.liquorclient.event.EventManager;
import com.example.liquorclient.event.Subscribe;
import com.example.liquorclient.event.impl.MotionEvent;
import com.example.liquorclient.event.impl.PacketEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

public class RotationManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static float yaw, pitch;
    private static boolean rotating;

    public static void init() {
        EventManager.subscribe(new RotationManager());
    }

    public static void setRotations(float y, float p) {
        yaw = y;
        pitch = p;
        rotating = true;
    }

    public static void reset() {
        rotating = false;
    }

    @Subscribe(priority = 100)
    public void onMotion(MotionEvent.Pre event) {
        if (rotating) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @Subscribe
    public void onPacket(PacketEvent.Send event) {
        if (rotating && event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            // Force rotations into the packet if we are rotating silently
            // This is a simplified version, usually handled by mixin in ClientPlayerEntity
        }
    }

    public static float getYaw() { return rotating ? yaw : mc.player.getYaw(); }
    public static float getPitch() { return rotating ? pitch : mc.player.getPitch(); }
    public static boolean isRotating() { return rotating; }

    public static float[] getSmoothRotations(float[] current, float[] target, float speed) {
        float yaw = updateRotation(current[0], target[0], speed);
        float pitch = updateRotation(current[1], target[1], speed);
        return new float[]{yaw, pitch};
    }

    private static float updateRotation(float current, float target, float maxChange) {
        float f = MathHelper.wrapDegrees(target - current);
        if (f > maxChange) f = maxChange;
        if (f < -maxChange) f = -maxChange;
        return current + f;
    }
}
