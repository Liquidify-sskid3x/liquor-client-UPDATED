package com.example.liquorclient.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isMoving() {
        return mc.player != null && (mc.player.input.getMovementInput().x != 0 || mc.player.input.getMovementInput().y != 0);
    }

    public static void setSpeed(double speed) {
        if (mc.player == null) return;
        
        float forward = mc.player.input.getMovementInput().x;
        float side = mc.player.input.getMovementInput().y;
        float yaw = mc.player.getYaw();

        if (forward == 0 && side == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        } else {
            if (forward != 0) {
                if (side > 0) {
                    yaw += (forward > 0 ? -45 : 45);
                } else if (side < 0) {
                    yaw += (forward > 0 ? 45 : -45);
                }
                side = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            
            double sin = Math.sin(Math.toRadians(yaw + 90.0f));
            double cos = Math.cos(Math.toRadians(yaw + 90.0f));
            mc.player.setVelocity(forward * speed * cos + side * speed * sin, mc.player.getVelocity().y, forward * speed * sin - side * speed * cos);
        }
    }

    public static void rotate(float yaw, float pitch, boolean packet) {
        if (mc.player == null) return;
        
        if (packet) {
            PacketUtils.send(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
        } else {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
    }

    public static float[] getRotationsNeeded(Vec3d target) {
        if (mc.player == null) return new float[]{0, 0};
        
        Vec3d diff = target.subtract(mc.player.getEyePos());
        double diffX = diff.x;
        double diffY = diff.y;
        double diffZ = diff.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]{
            mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
            mc.player.getPitch() + MathHelper.wrapDegrees(pitch - mc.player.getPitch())
        };
    }

    public static int getSlotWithItem(ItemStack stack) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == stack.getItem()) return i;
        }
        return -1;
    }
}
