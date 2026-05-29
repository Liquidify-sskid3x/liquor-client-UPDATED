package com.example.liquorclient.module;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, Double value, double min, double max) {
        this(name, value, min, max, calculateDefaultStep(min, max));
    }

    public NumberSetting(String name, Double value, double min, double max, double step) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.step = step <= 0.0 ? 1.0 : step;
    }

    private static double calculateDefaultStep(double min, double max) {
        double range = max - min;
        if (range <= 1.01) {
            return 0.01;
        } else if (range <= 5.01) {
            return 0.1;
        } else if (range <= 10.01) {
            return 0.1;
        } else {
            return 1.0;
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    @Override
    public void setValue(Double value) {
        super.setValue(snap(value));
    }

    public double snap(double value) {
        double clamped = Math.max(min, Math.min(max, value));
        double snapped = min + Math.round((clamped - min) / step) * step;
        return Math.max(min, Math.min(max, snapped));
    }
}
