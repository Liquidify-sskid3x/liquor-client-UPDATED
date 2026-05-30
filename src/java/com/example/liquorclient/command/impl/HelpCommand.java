package com.example.liquorclient.command.impl;

import com.example.liquorclient.command.Command;
import com.example.liquorclient.command.Commands;
import com.example.liquorclient.utility.ChatUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Displays all available commands.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatUtils.message("Available commands:");
            for (Command command : Commands.getCommands()) {
                ChatUtils.message(". " + command.getName() + " - " + command.getDescription());
            }
            return 1;
        });
    }
}
