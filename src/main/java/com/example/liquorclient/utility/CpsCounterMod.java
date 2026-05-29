package com.example.liquorclient.utility;

import com.example.liquorclient.gui.ArrayListHud;
import com.example.liquorclient.module.BooleanSetting;
import com.example.liquorclient.module.Category;
import com.example.liquorclient.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsCounterMod extends Module {
    private final BooleanSetting showRight = new BooleanSetting("Show Right", true);
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean lastLeftDown = false;
    private boolean lastRightDown = false;

    public CpsCounterMod() {
        super("CPS Counter", Category.RENDER, "Shows your clicks per second");
        addSetting(showRight);

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private void tick(MinecraftClient mc) {
        if (mc.getWindow() == null) return;
        long window = mc.getWindow().getHandle();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();

        if (leftDown && !lastLeftDown) leftClicks.addLast(now);
        if (rightDown && !lastRightDown) rightClicks.addLast(now);
        lastLeftDown = leftDown;
        lastRightDown = rightDown;
        prune(leftClicks, now);
        prune(rightClicks, now);
    }

    private void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.options.hudHidden) return;

        int drawX = ArrayListHud.getCpsX();
        int drawY = ArrayListHud.getCpsY();
        String text = "LMB " + leftClicks.size() + " CPS";
        if (showRight.getValue()) {
            text += " | RMB " + rightClicks.size() + " CPS";
        }
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(text), drawX, drawY, 0xFFFFFFFF);
    }

    private void prune(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) {
            clicks.removeFirst();
        }
    }
}
