package com.example.liquorclient.combat;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.hit.HitResult;

import java.util.concurrent.ThreadLocalRandom;

public class WTapMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final NumberSetting chance = new NumberSetting("Chance", 100.0, 0.0, 100.0);

    private int releaseTicksLeft = 0;
    private boolean restoreForward = false;

    public WTapMod() {
        super("W-Tap", Category.COMBAT, "Resets sprint on hit for increased knockback");
        addSetting(chance);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!isEnabled() || mc.player == null) {
                releaseTicksLeft = 0;
                return;
            }

            if (releaseTicksLeft <= 0) return;

            mc.options.forwardKey.setPressed(false);
            releaseTicksLeft--;

            if (releaseTicksLeft == 0) {
                mc.options.forwardKey.setPressed(restoreForward);
            }
        });
    }

    public void onSuccessfulAttack() {
        if (!isEnabled() || mc.player == null || releaseTicksLeft > 0) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
        if (ThreadLocalRandom.current().nextDouble(100.0) > chance.getValue()) return;

        restoreForward = isPhysicallyPressed(mc.options.forwardKey);
        releaseTicksLeft = ThreadLocalRandom.current().nextInt(1, 3);

        mc.player.setSprinting(false);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                    new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING)
            );
        }
        mc.options.forwardKey.setPressed(false);
    }

    private static boolean isPhysicallyPressed(KeyBinding binding) {
        if (mc.getWindow() == null) return false;
        InputUtil.Key key = InputUtil.fromTranslationKey(binding.getBoundKeyTranslationKey());
        return InputUtil.isKeyPressed(mc.getWindow(), key.getCode());
    }

    @Override
    public void onDisable() {
        releaseTicksLeft = 0;
        if (mc.options != null && restoreForward) {
            mc.options.forwardKey.setPressed(true);
        }
    }
}
