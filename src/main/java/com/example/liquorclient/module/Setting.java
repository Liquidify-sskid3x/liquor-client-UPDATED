package com.example.liquorclient.module;

import com.example.liquorclient.config.ConfigManager;

public abstract class Setting<T> {
    private final String name;
    private T value;
    private boolean visible = true;

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        ConfigManager.requestSave();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
