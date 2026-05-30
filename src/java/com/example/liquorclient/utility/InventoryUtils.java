package com.example.liquorclient.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void clickSlot(int slot, int button, SlotActionType actionType) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, button, actionType, mc.player);
    }

    public static void swap(int slot, int hotbarSlot) {
        clickSlot(slot, hotbarSlot, SlotActionType.SWAP);
    }

    public static int findItem(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < mc.player.currentScreenHandler.slots.size(); i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    public static int findItemInHotbar(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }
}
