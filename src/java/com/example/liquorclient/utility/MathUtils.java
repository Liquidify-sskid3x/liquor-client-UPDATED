package com.example.liquorclient.utility;

import net.minecraft.util.math.MathHelper;

public class MathUtils {
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static float clamp(float value, float min, float max) {
        return MathHelper.clamp(value, min, max);
    }

    public static double clamp(double value, double min, double max) {
        return MathHelper.clamp(value, min, max);
    }

    public static int clamp(int value, int min, int max) {
        return MathHelper.clamp(value, min, max);
    }
}
