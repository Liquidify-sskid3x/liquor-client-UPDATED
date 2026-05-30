package com.example.liquorclient.combat;

import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.ThreadLocalRandom;

public class VelocityMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final NumberSetting chance = new NumberSetting("Chance", 100.0, 0.0, 100.0);

    private int lastHurtTime = 0;
    private boolean jumpedThisHurt = false;

    public VelocityMod() {
        super("Velocity", Category.COMBAT, "Reduces or removes knockback taken");
        addSetting(chance);
    }

    @com.example.liquorclient.event.Subscribe
    private void onTick(com.example.liquorclient.event.impl.TickEvent.Pre event) {
        if (mc.player == null) {
            lastHurtTime = 0;
            jumpedThisHurt = false;
            return;
        }

        int hurtTime = mc.player.hurtTime;
        if (hurtTime == 0) {
            jumpedThisHurt = false;
        } else if (hurtTime > 0 && lastHurtTime == 0) {
            tryJumpReset();
        }
        lastHurtTime = hurtTime;
    }

    public void onPlayerVelocity() {
        if (!isEnabled() || mc.player == null) return;
        tryJumpReset();
    }

    private void tryJumpReset() {
        if (jumpedThisHurt) return;
        if (!mc.player.isOnGround()) return;
        if (ThreadLocalRandom.current().nextDouble(100.0) > chance.getValue()) return;

        jumpedThisHurt = true;
        mc.player.jump();
    }

    @Override
    public void onDisable() {
        lastHurtTime = 0;
        jumpedThisHurt = false;
    }
}
