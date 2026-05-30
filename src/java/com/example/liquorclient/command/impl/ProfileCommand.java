package com.example.liquorclient.command.impl;

import com.example.liquorclient.command.Command;
import com.example.liquorclient.config.ConfigManager;
import com.example.liquorclient.utility.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

public class ProfileCommand extends Command {
    public ProfileCommand() {
        super("profile", "Manages client profiles.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("list")
            .executes(context -> {
                ChatUtils.message("Profiles: " + String.join(", ", ConfigManager.getProfiles()));
                return 1;
            })
        );

        builder.then(LiteralArgumentBuilder.<CommandSource>literal("use")
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.string())
                .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(ConfigManager.getProfiles(), suggestionsBuilder))
                .executes(context -> {
                    String name = StringArgumentType.getString(context, "name");
                    if (ConfigManager.switchProfile(name)) {
                        ChatUtils.message("Loaded profile: " + name);
                        return 1;
                    } else {
                        ChatUtils.error("Could not load profile: " + name);
                        return 0;
                    }
                })
            )
        );

        builder.then(LiteralArgumentBuilder.<CommandSource>literal("create")
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.string())
                .executes(context -> {
                    String name = StringArgumentType.getString(context, "name");
                    if (ConfigManager.createProfile(name)) {
                        ChatUtils.message("Created profile: " + name);
                        return 1;
                    } else {
                        ChatUtils.error("Could not create profile: " + name);
                        return 0;
                    }
                })
            )
        );
        
        // Add delete, export, import similarly if needed
    }
}
