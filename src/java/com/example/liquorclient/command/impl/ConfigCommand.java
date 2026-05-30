package com.example.liquorclient.command.impl;

import com.example.liquorclient.command.Command;
import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.utility.ChatUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", "Manages client configuration.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("save")
            .executes(context -> {
                ConfigManager.saveNow();
                ChatUtils.message("Saved config to " + ConfigManager.getConfigDir());
                return 1;
            })
        );
    }
}
