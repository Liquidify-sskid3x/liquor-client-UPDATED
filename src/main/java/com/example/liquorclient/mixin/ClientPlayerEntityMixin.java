package com.example.liquorclient.mixin;

import com.example.liquorclient.combat.ReachMod;
import com.example.liquorclient.combat.TickBaseMod;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
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
}
