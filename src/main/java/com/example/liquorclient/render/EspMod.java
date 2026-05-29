package com.example.liquorclient.render;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.NumberSetting;

public class EspMod extends Module {
    private final NumberSetting range = new NumberSetting("Range", 64.0, 10.0, 256.0);
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private final BooleanSetting filled = new BooleanSetting("Filled", false);
    private final BooleanSetting names = new BooleanSetting("Names", true);
    private final BooleanSetting healthBar = new BooleanSetting("Health Bar", true);
    private final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);

    public EspMod() {
        super("2D ESP", Category.RENDER, "Shows entities through walls with 2D boxes");
        addSetting(range);
        addSetting(players);
        addSetting(mobs);
        addSetting(filled);
        addSetting(names);
        addSetting(healthBar);
        addSetting(rainbow);
    }

    public double getRange() {
        return range.getValue();
    }

    public boolean shouldRenderPlayers() {
        return players.getValue();
    }

    public boolean shouldRenderMobs() {
        return mobs.getValue();
    }

    public boolean shouldFillBoxes() {
        return filled.getValue();
    }

    public boolean shouldShowNames() {
        return names.getValue();
    }

    public boolean shouldShowHealthBar() {
        return healthBar.getValue();
    }

    public boolean shouldUseRainbow() {
        return rainbow.getValue();
    }
}
