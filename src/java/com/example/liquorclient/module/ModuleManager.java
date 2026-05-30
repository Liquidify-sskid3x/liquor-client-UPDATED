package com.example.liquorclient.module;

import com.example.liquorclient.aimassist.AimAssistMod;
import com.example.liquorclient.autokillaura.TriggerBotMod;
import com.example.liquorclient.developer.DeveloperPanelMod;
import com.example.liquorclient.misc.AutoGGMod;
import com.example.liquorclient.player.*;
import com.example.liquorclient.scaffold.ScaffoldMod;
import com.example.liquorclient.utility.*;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        register(new TriggerBotMod());
        register(new com.example.liquorclient.combat.LeftClickerMod());
        register(new com.example.liquorclient.combat.RightClickerMod());
        register(new com.example.liquorclient.combat.ReachMod());
        register(new com.example.liquorclient.combat.FastPlaceMod());
        register(new com.example.liquorclient.combat.VelocityMod());
        register(new com.example.liquorclient.combat.WTapMod());
        register(new com.example.liquorclient.combat.BacktrackMod());
        register(new com.example.liquorclient.combat.HitboxMod());
        register(new com.example.liquorclient.combat.FakeLagMod());
        register(new com.example.liquorclient.combat.TickBaseMod());
        register(new SprintMod());
        register(new ScaffoldMod());
        register(new AimAssistMod());
        register(new ChestStealerMod());
        register(new AutoGGMod());
        register(new com.example.liquorclient.render.ClickGuiMod());
        register(new com.example.liquorclient.render.EspMod());
        register(new com.example.liquorclient.render.ItemEspMod());
        register(new com.example.liquorclient.render.ChestEspMod());
        register(new com.example.liquorclient.render.BetterTooltipsMod());
        register(new com.example.liquorclient.render.ReachDisplayMod());
        register(new ZoomMod());
        register(new FullbrightMod());
        register(new ArmorHudMod());
        register(new PotionHudMod());
        register(new KeystrokesMod());
        register(new CpsCounterMod());
        register(new DeveloperPanelMod());
    }

    public static void register(Module m){
        modules.add(m);
    }

    public static List<Module> getByCategory(Category cat){
        return modules.stream()
                .filter(m -> m.getCategory() == cat)
                .sorted(Comparator.comparing(Module::getName))
                .collect(Collectors.toList());
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static <T extends Module> T getModule(Class<T> type) {
        for (Module module : modules) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        return null;
    }
}
