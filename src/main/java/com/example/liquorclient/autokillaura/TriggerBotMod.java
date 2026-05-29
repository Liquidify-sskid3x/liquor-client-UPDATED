package com.example.liquorclient.autokillaura;

import com.example.liquorclient.mixin.MinecraftClientAccessor;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.NumberSetting;
import com.example.liquorclient.module.Setting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TriggerBotMod extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final NumberSetting minDelay = new NumberSetting("Min Delay", 100.0, 0.0, 1000.0);
    private final NumberSetting maxDelay = new NumberSetting("Max Delay", 200.0, 0.0, 1000.0);
    private final BooleanSetting combat18 = new BooleanSetting("1.8 Combat", false);
    private final BooleanSetting autoblock18 = new BooleanSetting("Autoblock 1.8", false);
    private final NumberSetting minCps = new NumberSetting("Min CPS", 8.0, 1.0, 20.0);
    private final NumberSetting maxCps = new NumberSetting("Max CPS", 12.0, 1.0, 20.0);
    private final BooleanSetting smoothCps = new BooleanSetting("Smooth CPS", true);
    private final NumberSetting smoothSpeed = new NumberSetting("Smooth Speed", 0.12, 0.02, 0.5);
    private final BooleanSetting focusTarget = new BooleanSetting("Focus Target", true);
    private final BooleanSetting debug = new BooleanSetting("Debug", false);

    private Entity lastTarget = null;
    private long lastAttackTime = 0L;
    private long lastDebugTime = 0L;
    private long nextCpsRollTime = 0L;
    private double currentCps = 10.0;
    private double targetCps = 10.0;
    private int debugAttempts = 0;
    private int debugSkips = 0;

    public TriggerBotMod() {
        super("Trigger Bot", Category.COMBAT, "Auto-attacks when aiming at entities");
        addSetting(minDelay);
        addSetting(maxDelay);
        addSetting(combat18);
        addSetting(autoblock18);
        addSetting(minCps);
        addSetting(maxCps);
        addSetting(smoothCps);
        addSetting(smoothSpeed);
        addSetting(focusTarget);
        addSetting(debug);
        updateSettingVisibility();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            updateSettingVisibility();
            if (!isEnabled()) return;
            if (mc.player == null || mc.interactionManager == null) {
                debugSkip("no player or interaction manager", 0.0, 0L, "none");
                return;
            }
            if (mc.currentScreen != null) {
                debugSkip("screen open", 0.0, 0L, "none");
                return;
            }

            long now = System.currentTimeMillis();
            boolean legacyMode = combat18.getValue() || autoblock18.getValue();
            double delay = legacyMode ? 1000.0 / nextCombatCps(now) : nextDelay();
            long sinceLast = now - lastAttackTime;
            if (sinceLast < delay) {
                debugSkip("waiting delay", delay, sinceLast, targetName());
                return;
            }

            if (mc.crosshairTarget instanceof EntityHitResult entityHit) {
                Entity target = entityHit.getEntity();
                
                if (focusTarget.getValue() && lastTarget != null && lastTarget.isAlive() && target != lastTarget) {
                    if (mc.player.squaredDistanceTo(lastTarget) < 40.0) { // Keep focus if old target is nearby
                        debugSkip("focusing old target", delay, sinceLast, target.getName().getString());
                        return;
                    } else {
                        lastTarget = null; // Reset focus if old target is too far
                    }
                }

                if (target.isAlive() && target instanceof LivingEntity) {
                    float cooldown = mc.player.getAttackCooldownProgress(0.0f);
                    if (legacyMode || cooldown >= 1.0f) {
                        if (!performVanillaAttack(legacyMode)) {
                            debugSkip("doAttack rejected", delay, sinceLast, target.getName().getString());
                            return;
                        }
                        debugAttempts++;
                        lastTarget = target;

                        if (autoblock18.getValue()) {
                            MinecraftClientAccessor accessor = (MinecraftClientAccessor) mc;
                            accessor.liquor$setItemUseCooldown(0);
                            accessor.liquor$doItemUse();
                        }

                        lastAttackTime = now;
                        debugAttack(target.getName().getString(), delay, sinceLast, cooldown, legacyMode);
                    } else {
                        debugSkip("cooldown " + format(cooldown), delay, sinceLast, target.getName().getString());
                    }
                } else {
                    debugSkip("target not living/alive", delay, sinceLast, target.getName().getString());
                }
            } else {
                debugSkip("no entity crosshair target", delay, sinceLast, "none");
            }
        });
    }

    @Override
    public List<Setting<?>> getSettings() {
        updateSettingVisibility();
        return super.getSettings();
    }

    private void updateSettingVisibility() {
        boolean use18 = combat18.getValue() || autoblock18.getValue();
        minDelay.setVisible(!use18);
        maxDelay.setVisible(!use18);
        minCps.setVisible(use18);
        maxCps.setVisible(use18);
        smoothCps.setVisible(use18);
        smoothSpeed.setVisible(use18 && smoothCps.getValue());
    }

    private double nextDelay() {
        double min = minDelay.getValue();
        double max = maxDelay.getValue();
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        return min == max ? min : ThreadLocalRandom.current().nextDouble(min, max);
    }

    private double nextCombatCps(long now) {
        double min = Math.max(1.0, minCps.getValue());
        double max = Math.max(1.0, maxCps.getValue());
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        if (!smoothCps.getValue()) {
            return randomCps(min, max);
        }

        if (currentCps < min || currentCps > max) {
            currentCps = randomCps(min, max);
            targetCps = currentCps;
        }

        if (now >= nextCpsRollTime) {
            targetCps = randomCps(min, max);
            nextCpsRollTime = now + ThreadLocalRandom.current().nextLong(350L, 850L);
        }

        double speed = Math.max(0.02, Math.min(0.5, smoothSpeed.getValue()));
        currentCps += (targetCps - currentCps) * speed;
        return Math.max(min, Math.min(max, currentCps));
    }

    private double randomCps(double min, double max) {
        return min == max ? min : ThreadLocalRandom.current().nextDouble(min, max);
    }

    private boolean performVanillaAttack(boolean legacyMode) {
        MinecraftClientAccessor accessor = (MinecraftClientAccessor) mc;
        if (legacyMode) {
            accessor.liquor$setAttackCooldown(0);
        }
        return accessor.liquor$doAttack();
    }

    private void debugSkip(String reason, double delay, long sinceLast, String target) {
        debugSkips++;
        debugStatus(reason, delay, sinceLast, target);
    }

    private void debugAttack(String target, double delay, long sinceLast, float cooldown, boolean legacyMode) {
        if (!debug.getValue()) return;
        debugLine("attack target=" + target
                + " mode=" + (legacyMode ? "1.8" : "modern")
                + " delay=" + format(delay) + "ms"
                + " since=" + sinceLast + "ms"
                + " cooldown=" + format(cooldown)
                + " autoblock=" + autoblock18.getValue());
    }

    private void debugStatus(String reason, double delay, long sinceLast, String target) {
        if (!debug.getValue()) return;
        long now = System.currentTimeMillis();
        if (now - lastDebugTime < 1000L) return;
        lastDebugTime = now;

        debugLine("skip=" + reason
                + " target=" + target
                + " attempts/s=" + debugAttempts
                + " skips/s=" + debugSkips
                + " delay=" + format(delay) + "ms"
                + " since=" + sinceLast + "ms"
                + " cps=" + format(currentCps));
        debugAttempts = 0;
        debugSkips = 0;
    }

    private void debugLine(String message) {
        if (mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(Text.literal("[TriggerBot Debug] " + message));
        }
    }

    private String targetName() {
        if (mc.crosshairTarget instanceof EntityHitResult entityHit) {
            return entityHit.getEntity().getName().getString();
        }
        return "none";
    }

    private String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    @Override
    public void onEnable() {
        lastDebugTime = 0L;
        debugAttempts = 0;
        debugSkips = 0;
    }

    @Override
    public void onDisable() {
        lastTarget = null;
    }
}
