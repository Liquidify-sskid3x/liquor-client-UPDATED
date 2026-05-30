package com.example.liquorclient.aimassist;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.utility.FriendManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class AimAssistMod extends Module {
    // Porting this from 20Hz ticks to FPS-based rendering events was tricky.
    // Finding the right event (BEFORE_ENTITIES) in this API version
    // required some research into the render pipeline
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    private final NumberSetting speed = new NumberSetting("Speed", 0.5, 0.01, 1.0);
    private final NumberSetting range = new NumberSetting("Range", 5.0, 1.0, 10.0);
    private final BooleanSetting holdToAim = new BooleanSetting("Hold to Aim", true);
    private final BooleanSetting wallCheck = new BooleanSetting("Wall Check", true);

    public AimAssistMod() {
        super("Aim Assist", Category.COMBAT, "Gently aims at nearby entities");
        addSetting(speed);
        addSetting(range);
        addSetting(holdToAim);
        addSetting(wallCheck);

        // Run at frame-rate (FPS) instead of Tick-rate (20Hz)
        WorldRenderEvents.BEFORE_ENTITIES.register(context -> {
            if (!isEnabled() || mc.player == null || mc.world == null || mc.currentScreen != null) return;
            if (holdToAim.getValue() && !mc.options.attackKey.isPressed()) return;

            PlayerEntity target = getClosestTarget();
            if (target != null) {
                smoothAim(target);
            }
        });
    }

    private void smoothAim(PlayerEntity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getStandingEyeHeight() * 0.85) - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double dz = target.getZ() - mc.player.getZ();

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, distXZ)));

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float deltaPitch = targetPitch - mc.player.getPitch();

        // Only assist if target is in a reasonable FOV (90 degrees)
        if (Math.abs(deltaYaw) < 45 && Math.abs(deltaPitch) < 45) {
            // Apply smoothing based on speed setting
            // Scale by 0.1 because frames update much faster than ticks
            float s = speed.getValue().floatValue() * 0.1f;
            
            float yawMove = deltaYaw * s;
            float pitchMove = deltaPitch * s;

            mc.player.setYaw(mc.player.getYaw() + yawMove);
            mc.player.setPitch(mc.player.getPitch() + pitchMove);
            
            // Keep head and body in sync
            mc.player.headYaw = mc.player.getYaw();
            mc.player.bodyYaw = mc.player.getYaw();
        }
    }

    private PlayerEntity getClosestTarget() {
        PlayerEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        double maxRangeSq = range.getValue() * range.getValue();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive() || player.isInvisible()) continue;
            if (FriendManager.isFriend(player)) continue;
            if (wallCheck.getValue() && !mc.player.canSee(player)) continue;

            double dist = mc.player.squaredDistanceTo(player);
            if (dist <= maxRangeSq && dist < closestDist) {
                closest = player;
                closestDist = dist;
            }
        }
        return closest;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
