package com.example.liquorclient.mixin;

import com.example.liquorclient.combat.HitboxMod;
import com.example.liquorclient.module.ModuleManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void liquor$onGetTargetingMargin(CallbackInfoReturnable<Float> cir) {
        HitboxMod hitbox = ModuleManager.getModule(HitboxMod.class);
        if (hitbox != null && hitbox.isEnabled()) {
            cir.setReturnValue(hitbox.getExpansion());
        }
    }
}
