package com.example.liquorclient.command.impl;

import com.example.liquorclient.command.Command;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.utility.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module on or off.", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<CommandSource, String>argument("module", StringArgumentType.string())
            .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(ModuleManager.getModules().stream().map(Module::getName), suggestionsBuilder))
            .executes(context -> {
                String moduleName = StringArgumentType.getString(context, "module");
                for (Module module : ModuleManager.getModules()) {
                    if (module.getName().equalsIgnoreCase(moduleName)) {
                        module.toggle();
                        return 1;
                    }
                }
                ChatUtils.error("Module not found: " + moduleName);
                return 0;
            })
        );
    }
}
