package com.example.liquorclient.mixin;

import com.example.liquorclient.command.Commands;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class CommandSuggestorMixin {
    @Shadow @Final TextFieldWidget textField;
    @Shadow @Final MinecraftClient client;
    @Shadow private ParseResults<CommandSource> parse;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow protected abstract void show(boolean narrateFirstSuggestion);

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void onRefresh(CallbackInfo ci) {
        String text = textField.getText();
        if (text.startsWith(".")) {
            int cursor = textField.getCursor();
            StringReader reader = new StringReader(text.substring(1));
            
            CommandSource source = client.getNetworkHandler().getCommandSource();
            this.parse = Commands.DISPATCHER.parse(reader, source);
            
            this.pendingSuggestions = Commands.DISPATCHER.getCompletionSuggestions(this.parse, cursor);
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    this.show(false);
                }
            });
            
            ci.cancel();
        }
    }
}
