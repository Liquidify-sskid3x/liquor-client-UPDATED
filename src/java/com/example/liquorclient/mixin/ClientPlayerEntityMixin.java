package com.example.liquorclient.mixin;

import com.example.liquorclient.combat.ReachMod;
import com.example.liquorclient.combat.TickBaseMod;
import com.example.liquorclient.event.EventManager;
import com.example.liquorclient.event.impl.MotionEvent;
import com.example.liquorclient.utility.RotationManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends Entity {
    public ClientPlayerEntityMixin(net.minecraft.entity.EntityType<?> type, net.minecraft.world.World world) {
        super(type, world);
    }

    @Shadow protected abstract void sendMovementPackets();

    private float liquor$originalYaw, liquor$originalPitch;

    @ModifyArg(
            method = "method_76762",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;method_76763(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"
            ),
            index = 2
    )
    private double liquor$applyReach(double vanillaRange) {
        return ReachMod.getEffectiveRange(vanillaRange);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void liquor$onTick(CallbackInfo ci) {
        if (TickBaseMod.shouldSkipTick()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void liquor$onSendMovementPacketsPre(CallbackInfo ci) {
        liquor$originalYaw = getYaw();
        liquor$originalPitch = getPitch();
        
        MotionEvent.Pre event = EventManager.post(new MotionEvent.Pre(getYaw(), getPitch(), isOnGround()));
        if (event.isCancelled()) return;
        
        if (RotationManager.isRotating()) {
            setYaw(event.getYaw());
            setPitch(event.getPitch());
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void liquor$onSendMovementPacketsPost(CallbackInfo ci) {
        setYaw(liquor$originalYaw);
        setPitch(liquor$originalPitch);
        EventManager.post(new MotionEvent.Post(getYaw(), getPitch(), isOnGround()));
    }
}
