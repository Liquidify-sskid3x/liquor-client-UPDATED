package com.example.liquorclient.mixin;

import com.example.liquorclient.combat.BacktrackMod;
import com.example.liquorclient.combat.WTapMod;
import com.example.liquorclient.event.EventManager;
import com.example.liquorclient.event.impl.TickEvent;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.render.ReachDisplayMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void liquor$onTickPre(CallbackInfo ci) {
        EventManager.post(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void liquor$onTickPost(CallbackInfo ci) {
        EventManager.post(new TickEvent.Post());
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void liquor$onDoAttackHead(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
        EntityHitResult ehr = (EntityHitResult) mc.crosshairTarget;
        double dist = mc.player != null ? mc.player.distanceTo(ehr.getEntity()) : 0;
        ReachDisplayMod rd = ModuleManager.getModule(ReachDisplayMod.class);
        if (rd != null) rd.onAttack(dist);
    }

    @Inject(method = "doAttack", at = @At("RETURN"))
    private void liquor$onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        WTapMod wTap = ModuleManager.getModule(WTapMod.class);
        if (wTap != null) {
            wTap.onSuccessfulAttack();
        }
        BacktrackMod backtrack = ModuleManager.getModule(BacktrackMod.class);
        if (backtrack != null) {
            backtrack.onSuccessfulAttack();
        }
    }
}
