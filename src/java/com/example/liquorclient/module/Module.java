package com.example.liquorclient.module;

import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.gui.NotificationManager;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {

    private final String name;
    private final Category category;
    private boolean enabled;
    private final String description;

    private final List<Setting<?>> settings = new ArrayList<>();
    private final KeybindSetting keybind = new KeybindSetting();

    public Module(String name, Category category){
        this(name, category, "");
    }

    public Module(String name, Category category, String description){
        this.name = name;
        this.category = category;
        this.description = description;
        this.settings.add(keybind);
    }

    protected void addSetting(Setting<?> setting) {
        // Insert before keybind so keybind is always at the bottom
        settings.add(settings.size() - 1, setting);
    }

    public void toggle(){
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        setEnabled(enabled, true);
    }

    public void setEnabledSilently(boolean enabled) {
        setEnabled(enabled, false);
    }

    private void setEnabled(boolean enabled, boolean notify) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;
        if (enabled) {
            com.example.liquorclient.event.EventManager.subscribe(this);
            onEnable();
        } else {
            com.example.liquorclient.event.EventManager.unsubscribe(this);
            onDisable();
        }

        if (notify) {
            NotificationManager.push(name, enabled ? "Enabled" : "Disabled");
        }
        ConfigManager.requestSave();
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public boolean isEnabled(){
        return enabled;
    }

    public String getName(){
        return name;
    }

    public Category getCategory(){
        return category;
    }

    public List<Setting<?>> getSettings(){
        return settings;
    }

    public KeybindSetting getKeybind() {
        return keybind;
    }

    public String getDescription() {
        return description;
    }
}
