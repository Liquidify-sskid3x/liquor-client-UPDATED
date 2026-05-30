package com.example.liquorclient.mixin;

import com.example.liquorclient.combat.BacktrackMod;
import com.example.liquorclient.combat.FakeLagMod;
import com.example.liquorclient.module.ModuleManager;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Shadow @Final private NetworkSide side;

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void liquor$onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (side == NetworkSide.CLIENTBOUND) {
            BacktrackMod backtrack = ModuleManager.getModule(BacktrackMod.class);
            if (backtrack != null && backtrack.onReceivePacket(packet)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void liquor$onSend(Packet<?> packet, CallbackInfo ci) {
        if (side == NetworkSide.CLIENTBOUND) return;
        FakeLagMod fakeLag = ModuleManager.getModule(FakeLagMod.class);
        if (fakeLag != null && fakeLag.onSendPacket(packet)) {
            ci.cancel();
        }
    }
}
