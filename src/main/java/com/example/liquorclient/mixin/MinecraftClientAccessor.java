package com.example.liquorclient.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Invoker("doAttack")
    boolean liquor$doAttack();

    @Invoker("doItemUse")
    void liquor$doItemUse();

    @Accessor("itemUseCooldown")
    void liquor$setItemUseCooldown(int cooldown);

    @Accessor("attackCooldown")
    void liquor$setAttackCooldown(int cooldown);

    @Mutable
    @Accessor("session")
    void liquor$setSession(net.minecraft.client.session.Session session);
}
