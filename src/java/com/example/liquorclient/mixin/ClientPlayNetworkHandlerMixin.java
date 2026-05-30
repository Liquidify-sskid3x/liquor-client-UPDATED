package com.example.liquorclient.mixin;

import com.example.liquorclient.command.Commands;
import com.example.liquorclient.combat.VelocityMod;
import com.example.liquorclient.event.EventManager;
import com.example.liquorclient.event.impl.PacketEvent;
import com.example.liquorclient.misc.AutoGGMod;
import com.example.liquorclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void liquor$handleCommand(String message, CallbackInfo ci) {
        if (message.startsWith(".")) {
            com.example.liquorclient.command.Commands.dispatch(message.substring(1));
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At("TAIL"))
    private void liquor$onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        EventManager.post(new PacketEvent.Receive(packet));
        if (packet.overlay()) return;
        AutoGGMod autoGG = ModuleManager.getModule(AutoGGMod.class);
        if (autoGG != null) {
            autoGG.onGameMessage(packet.content().getString());
        }
    }

    @Inject(method = "onChatMessage", at = @At("TAIL"))
    private void liquor$onChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
        EventManager.post(new PacketEvent.Receive(packet));
        AutoGGMod autoGG = ModuleManager.getModule(AutoGGMod.class);
        if (autoGG != null) {
            autoGG.onGameMessage(packet.body().content());
        }
    }

    @Inject(method = "onProfilelessChatMessage", at = @At("TAIL"))
    private void liquor$onProfilelessChatMessage(ProfilelessChatMessageS2CPacket packet, CallbackInfo ci) {
        EventManager.post(new PacketEvent.Receive(packet));
        AutoGGMod autoGG = ModuleManager.getModule(AutoGGMod.class);
        if (autoGG != null) {
            autoGG.onGameMessage(packet.message().getString());
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("TAIL"))
    private void liquor$onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        EventManager.post(new PacketEvent.Receive(packet));
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || packet.getEntityId() != client.player.getId()) return;

        VelocityMod velocity = ModuleManager.getModule(VelocityMod.class);
        if (velocity == null) return;

        client.execute(velocity::onPlayerVelocity);
    }
}
