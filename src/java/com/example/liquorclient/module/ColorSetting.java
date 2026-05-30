package com.example.liquorclient.module;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int value) {
        super(name, value | 0xFF000000);
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(value == null ? 0xFFFFFFFF : value | 0xFF000000);
    }

    public int getRed() {
        return (getValue() >> 16) & 0xFF;
    }

    public int getGreen() {
        return (getValue() >> 8) & 0xFF;
    }

    public int getBlue() {
        return getValue() & 0xFF;
    }
}
