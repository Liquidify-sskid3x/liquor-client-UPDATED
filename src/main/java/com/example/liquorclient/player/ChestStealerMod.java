package com.example.liquorclient.player;

import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.concurrent.ThreadLocalRandom;

public class ChestStealerMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final NumberSetting minDelay = new NumberSetting("Min Delay", 100.0, 0.0, 1000.0);
    private final NumberSetting maxDelay = new NumberSetting("Max Delay", 200.0, 0.0, 1000.0);
    private final BooleanSetting autoClose = new BooleanSetting("Auto Close", true);
    private final BooleanSetting ignoreTrash = new BooleanSetting("Ignore Trash", true);

    private long lastStealTime = 0L;
    private long nextDelay = 0L;

    public ChestStealerMod() {
        super("ChestStealer", Category.PLAYER, "Automatically takes items from opened chests");
        addSetting(minDelay);
        addSetting(maxDelay);
        addSetting(autoClose);
        addSetting(ignoreTrash);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled() || mc.player == null || mc.world == null) return;
            if (!(mc.currentScreen instanceof GenericContainerScreen containerScreen)) return;

            GenericContainerScreenHandler handler = containerScreen.getScreenHandler();
            int containerSlots = handler.getInventory().size();

            long now = System.currentTimeMillis();
            if (now - lastStealTime < nextDelay) return;

            for (int i = 0; i < containerSlots; i++) {
                ItemStack stack = handler.getSlot(i).getStack();
                if (stack.isEmpty()) continue;
                if (ignoreTrash.getValue() && isTrash(stack)) continue;

                steal(handler.syncId, i);
                lastStealTime = now;
                nextDelay = calculateNextDelay();
                return;
            }

            if (autoClose.getValue()) {
                mc.player.closeHandledScreen();
            }
        });
    }

    private void steal(int syncId, int slotId) {
        if (mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.QUICK_MOVE, mc.player);
    }

    private long calculateNextDelay() {
        double min = minDelay.getValue();
        double max = maxDelay.getValue();
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return (long) (min == max ? min : ThreadLocalRandom.current().nextDouble(min, max));
    }

    private boolean isTrash(ItemStack stack) {
        String name = stack.getItem().getTranslationKey().toLowerCase();
        return name.contains("dirt") || name.contains("cobblestone") || name.contains("stone_button") 
            || name.contains("poisonous_potato") || name.contains("rotten_flesh") || name.contains("spider_eye");
    }

    @Override
    public void onEnable() {
        lastStealTime = 0L;
        nextDelay = 0L;
    }

    @Override
    public void onDisable() {
    }
}
