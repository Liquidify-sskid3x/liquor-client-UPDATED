package com.example.liquorclient.scaffold;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class ScaffoldMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean forcedSneak = false;
    private final NumberSetting edgeDistance = new NumberSetting("Edge Distance", 0.1, 0.01, 0.5);

    public ScaffoldMod() {
        super("Scaffold", Category.WORLD, "sneaks at the edge of blocks");
        addSetting(edgeDistance);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (mc.player == null || mc.world == null || !isEnabled()) {
                resetSneak();
                return;
            }

            if (!mc.player.isOnGround()) {
                resetSneak();
                return;
            }

            double edge = edgeDistance.getValue();
            boolean overAir = mc.world.getBlockState(BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ())).isAir();
            if (!overAir) {
                overAir = mc.world.getBlockState(BlockPos.ofFloored(mc.player.getX() + edge, mc.player.getY() - 0.5, mc.player.getZ())).isAir() ||
                          mc.world.getBlockState(BlockPos.ofFloored(mc.player.getX() - edge, mc.player.getY() - 0.5, mc.player.getZ())).isAir() ||
                          mc.world.getBlockState(BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ() + edge)).isAir() ||
                          mc.world.getBlockState(BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ() - edge)).isAir();
            }

            if (overAir) {
                mc.options.sneakKey.setPressed(true);
                forcedSneak = true;
            } else {
                resetSneak();
            }
        });
    }

    private void resetSneak() {
        if (forcedSneak) {
            mc.options.sneakKey.setPressed(false);
            forcedSneak = false;
        }
    }

    @Override
    public void onDisable() {
        resetSneak();
    }
}
