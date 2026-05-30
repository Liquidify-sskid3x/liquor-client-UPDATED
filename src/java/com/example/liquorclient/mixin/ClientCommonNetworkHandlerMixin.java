package com.example.liquorclient.mixin;

import com.example.liquorclient.event.EventManager;
import com.example.liquorclient.event.impl.PacketEvent;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void liquor$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Send event = EventManager.post(new PacketEvent.Send(packet));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
