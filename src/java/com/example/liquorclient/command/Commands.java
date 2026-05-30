package com.example.liquorclient.command;

import com.example.liquorclient.utility.ChatUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Commands {
    public static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    private static final List<Command> COMMANDS = new ArrayList<>();

    public static void init() {
        register(new com.example.liquorclient.command.impl.HelpCommand());
        register(new com.example.liquorclient.command.impl.ToggleCommand());
        register(new com.example.liquorclient.command.impl.BindCommand());
        register(new com.example.liquorclient.command.impl.SetCommand());
        register(new com.example.liquorclient.command.impl.ProfileCommand());
        register(new com.example.liquorclient.command.impl.ConfigCommand());
        register(new com.example.liquorclient.command.impl.DeveloperCommand());
    }

    public static void register(Command command) {
        COMMANDS.add(command);
        
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(command.getName());
        command.build(builder);
        DISPATCHER.register(builder);

        for (String alias : command.getAliases()) {
            LiteralArgumentBuilder<CommandSource> aliasBuilder = LiteralArgumentBuilder.literal(alias);
            command.build(aliasBuilder);
            DISPATCHER.register(aliasBuilder);
        }
    }

    public static void dispatch(String message) {
        try {
            DISPATCHER.execute(message, new LiquorCommandSource());
        } catch (CommandSyntaxException e) {
            ChatUtils.error(e.getMessage());
        } catch (Exception e) {
            ChatUtils.error("An error occurred while executing command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Command> getCommands() {
        return COMMANDS;
    }

    private static class LiquorCommandSource implements CommandSource {
        @Override
        public PermissionPredicate getPermissions() {
            return PermissionPredicate.ALL;
        }

        @Override
        public Collection<String> getPlayerNames() {
            return List.of();
        }

        @Override
        public Collection<String> getTeamNames() {
            return List.of();
        }

        @Override
        public Stream<Identifier> getSoundIds() {
            return Stream.of();
        }

        @Override
        public CompletableFuture<Suggestions> getCompletions(com.mojang.brigadier.context.CommandContext<?> context) {
            return Suggestions.empty();
        }

        @Override
        public Set<RegistryKey<World>> getWorldKeys() {
            return Set.of();
        }

        @Override
        public DynamicRegistryManager getRegistryManager() {
            return DynamicRegistryManager.EMPTY;
        }

        @Override
        public FeatureSet getEnabledFeatures() {
            return FeatureSet.empty();
        }

        @Override
        public CompletableFuture<Suggestions> listIdSuggestions(
                RegistryKey<? extends Registry<?>> registryRef,
                CommandSource.SuggestedIdType suggestedIdType,
                SuggestionsBuilder builder,
                com.mojang.brigadier.context.CommandContext<?> context) {
            return builder.buildFuture();
        }
    }
}
