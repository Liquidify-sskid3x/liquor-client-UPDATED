package com.example.liquorclient.command.impl;

import com.example.liquorclient.command.Command;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.Setting;
import com.example.liquorclient.utility.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

public class SetCommand extends Command {
    public SetCommand() {
        super("set", "Changes a module setting.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(RequiredArgumentBuilder.<CommandSource, String>argument("module", StringArgumentType.string())
            .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(ModuleManager.getModules().stream().map(Module::getName), suggestionsBuilder))
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("setting", StringArgumentType.string())
                .suggests((context, suggestionsBuilder) -> {
                    String moduleName = StringArgumentType.getString(context, "module");
                    for (Module m : ModuleManager.getModules()) {
                        if (m.getName().equalsIgnoreCase(moduleName)) {
                            return CommandSource.suggestMatching(m.getSettings().stream().map(Setting::getName), suggestionsBuilder);
                        }
                    }
                    return suggestionsBuilder.buildFuture();
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("value", StringArgumentType.greedyString())
                    .executes(context -> {
                        String moduleName = StringArgumentType.getString(context, "module");
                        String settingName = StringArgumentType.getString(context, "setting");
                        String value = StringArgumentType.getString(context, "value");
                        
                        Module module = null;
                        for (Module m : ModuleManager.getModules()) {
                            if (m.getName().equalsIgnoreCase(moduleName)) {
                                module = m;
                                break;
                            }
                        }
                        
                        if (module == null) {
                            ChatUtils.error("Module not found: " + moduleName);
                            return 0;
                        }
                        
                        Setting<?> setting = null;
                        for (Setting<?> s : module.getSettings()) {
                            if (s.getName().equalsIgnoreCase(settingName)) {
                                setting = s;
                                break;
                            }
                        }
                        
                        if (setting == null) {
                            ChatUtils.error("Setting not found: " + settingName);
                            return 0;
                        }
                        
                        applySetting(setting, value);
                        ChatUtils.message(module.getName() + " " + setting.getName() + " set to " + setting.getValue());
                        return 1;
                    })
                )
            )
        );
    }

    private void applySetting(Setting<?> setting, String value) {
        try {
            if (setting instanceof BooleanSetting booleanSetting) {
                booleanSetting.setValue(Boolean.parseBoolean(value));
            } else if (setting instanceof NumberSetting numberSetting) {
                numberSetting.setValue(Double.parseDouble(value));
            }
            // Add other setting types if needed
        } catch (Exception e) {
            ChatUtils.error("Invalid value: " + value);
        }
    }
}
