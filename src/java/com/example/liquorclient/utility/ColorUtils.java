package com.example.liquorclient.utility;

public class ColorUtils {
    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int setAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static int blend(int color1, int color2, float ratio) {
        ratio = MathUtils.clamp(ratio, 0, 1);
        int a = (int) (getAlpha(color1) * (1 - ratio) + getAlpha(color2) * ratio);
        int r = (int) (getRed(color1) * (1 - ratio) + getRed(color2) * ratio);
        int g = (int) (getGreen(color1) * (1 - ratio) + getGreen(color2) * ratio);
        int b = (int) (getBlue(color1) * (1 - ratio) + getBlue(color2) * ratio);
        return argb(a, r, g, b);
    }

    public static int rainbow(float speed, float saturation, float brightness, long offset) {
        float hue = ((System.currentTimeMillis() + offset) % (int) (speed * 1000)) / (speed * 1000);
        return java.awt.Color.HSBtoRGB(hue, saturation, brightness) | 0xFF000000;
    }

    public static int rainbow(float speed, float saturation) {
        return rainbow(speed, saturation, 1.0f, 0);
    }

    public static int darken(int color, float factor) {
        factor = MathUtils.clamp(factor, 0, 1);
        int r = (int) (getRed(color) * factor);
        int g = (int) (getGreen(color) * factor);
        int b = (int) (getBlue(color) * factor);
        return rgba(r, g, b, getAlpha(color));
    }

    public static int lighten(int color, float factor) {
        factor = MathUtils.clamp(factor, 0, 1);
        int r = (int) (getRed(color) + (255 - getRed(color)) * factor);
        int g = (int) (getGreen(color) + (255 - getGreen(color)) * factor);
        int b = (int) (getBlue(color) + (255 - getBlue(color)) * factor);
        return rgba(r, g, b, getAlpha(color));
    }

    public static int multiply(int color, float factor) {
        int r = (int) Math.min(255, getRed(color) * factor);
        int g = (int) Math.min(255, getGreen(color) * factor);
        int b = (int) Math.min(255, getBlue(color) * factor);
        return rgba(r, g, b, getAlpha(color));
    }

    public static int gradient(int start, int end, float progress) {
        return blend(start, end, progress);
    }

    public static int[] toRGBAArray(int color) {
        return new int[]{getRed(color), getGreen(color), getBlue(color), getAlpha(color)};
    }

    public static int toDecimal(float r, float g, float b, float a) {
        return argb((int) (a * 255), (int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    public static int withAlpha(int color, float alpha) {
        return setAlpha(color, (int) (alpha * 255));
    }
}
