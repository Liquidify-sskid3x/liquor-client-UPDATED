package com.example.liquorclient.command.impl;

import com.example.liquorclient.command.Command;
import com.example.liquorclient.gui.DeveloperScreen;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

public class DeveloperCommand extends Command {
    public DeveloperCommand() {
        super("developer", "Opens the developer screen.", "dev");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.execute(() -> mc.setScreen(new DeveloperScreen(mc.currentScreen)));
            return 1;
        });
    }
}
