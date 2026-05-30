package com.example.liquorclient.utility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class WorldUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Block getBlock(BlockPos pos) {
        if (mc.world == null) return Blocks.AIR;
        return mc.world.getBlockState(pos).getBlock();
    }

    public static BlockState getBlockState(BlockPos pos) {
        if (mc.world == null) return Blocks.AIR.getDefaultState();
        return mc.world.getBlockState(pos);
    }

    public static boolean isAir(BlockPos pos) {
        return getBlock(pos) == Blocks.AIR;
    }

    public static boolean isSolid(BlockPos pos) {
        return getBlockState(pos).isSolid();
    }
}
