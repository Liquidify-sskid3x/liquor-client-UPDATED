package com.example.liquorclient.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Accessor("blockEntities")
    Set<BlockEntity> liquor$getBlockEntities();
}
