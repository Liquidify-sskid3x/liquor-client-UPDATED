package com.example.liquorclient.mixin;

import com.example.liquorclient.utility.ZoomMod;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void liquor$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ZoomMod.onScroll(vertical)) {
            ci.cancel();
        }
    }
}
