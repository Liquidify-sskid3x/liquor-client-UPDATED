package com.example.liquorclient.combat;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SimulatedPlayer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final double GRAVITY = -0.08;
    private static final double AIR_FRICTION = 0.98;
    private static final double GROUND_FRICTION = 0.91;
    private static final double AIR_SPEED = 0.02;
    private static final double JUMP_VELOCITY = 0.42;

    public double x, y, z;
    public double motionX, motionY, motionZ;
    public float yaw;
    public boolean onGround;
    public float fallDistance;
    public boolean collidedHorizontally;
    public boolean sprinting;
    public boolean sneaking;

    private float forward, strafe;
    private boolean jumping;

    public SimulatedPlayer(ClientPlayerEntity player) {
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        Vec3d vel = player.getVelocity();
        this.motionX = vel.x;
        this.motionY = vel.y;
        this.motionZ = vel.z;
        this.yaw = player.getYaw();
        this.onGround = player.isOnGround();
        this.fallDistance = (float) player.fallDistance;
        this.collidedHorizontally = player.horizontalCollision;
        this.sprinting = player.isSprinting();
        this.sneaking = player.isSneaking();
        this.forward = player.input.getMovementInput().x;
        this.strafe = player.input.getMovementInput().y;
        this.jumping = mc.options.jumpKey.isPressed();
    }

    public void simulateTick() {
        applyInput();

        if (!onGround) {
            motionY += GRAVITY;
            motionY *= AIR_FRICTION;
        }

        move();

        applyFriction();

        clampVelocity();

        if (y < -64.0) return;
    }

    private void applyInput() {
        if (jumping && onGround) {
            motionY = JUMP_VELOCITY;
            if (sprinting) {
                float yawRad = yaw * (float) Math.PI / 180.0f;
                motionX -= Math.sin(yawRad) * 0.2;
                motionZ += Math.cos(yawRad) * 0.2;
            }
            onGround = false;
        }

        if (forward != 0.0f || strafe != 0.0f) {
            double speed = sprinting ? AIR_SPEED * 1.3 : AIR_SPEED;
            if (onGround) {
                speed = sprinting ? 0.16277136 * 1.3 : 0.16277136;
            }

            float yawRad = yaw * (float) Math.PI / 180.0f;
            double dx = (strafe * Math.cos(yawRad) - forward * Math.sin(yawRad)) * speed;
            double dz = (forward * Math.cos(yawRad) + strafe * Math.sin(yawRad)) * speed;
            motionX += dx;
            motionZ += dz;
        }
    }

    private void move() {
        if (sneaking && onGround) {
            motionX *= 0.3;
            motionZ *= 0.3;
        }

        x += motionX;
        y += motionY;
        z += motionZ;

        checkGroundCollision();

        if (motionY < 0 && onGround) {
            fallDistance = 0;
        } else if (motionY < 0) {
            fallDistance -= (float) motionY;
        }

        collidedHorizontally = false;
    }

    private void checkGroundCollision() {
        if (motionY > 0) return;
        BlockPos feetPos = BlockPos.ofFloored(x, y - 0.1, z);
        if (mc.world != null && !mc.world.getBlockState(feetPos).isAir()) {
            onGround = true;
            motionY = 0;
            y = feetPos.getY() + 1.0;
        } else {
            BlockPos belowPos = BlockPos.ofFloored(x, y - 0.5, z);
            if (mc.world != null && !mc.world.getBlockState(belowPos).isAir()) {
                onGround = true;
                motionY = 0;
                y = belowPos.getY() + 1.0;
            } else {
                onGround = false;
            }
        }
    }

    private void applyFriction() {
        double friction;
        if (onGround) {
            BlockPos pos = BlockPos.ofFloored(x, y - 0.1, z);
            if (mc.world != null) {
                friction = mc.world.getBlockState(pos).getBlock().getSlipperiness() * 0.91f;
            } else {
                friction = GROUND_FRICTION;
            }
        } else {
            friction = 0.98;
        }
        motionX *= friction;
        motionZ *= friction;
    }

    private void clampVelocity() {
        if (Math.abs(motionX) < 0.005) motionX = 0;
        if (Math.abs(motionY) < 0.005) motionY = 0;
        if (Math.abs(motionZ) < 0.005) motionZ = 0;
    }

    public Vec3d getPos() {
        return new Vec3d(x, y, z);
    }

    public double distanceTo(Vec3d other) {
        return getPos().distanceTo(other);
    }
}
