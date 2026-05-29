package com.example.liquorclient.gui;

import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.render.ChestEspMod;
import com.example.liquorclient.render.ClickGuiMod;
import com.example.liquorclient.render.EspMod;
import com.example.liquorclient.render.ItemEspMod;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ESPManager {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                render(drawContext, tickCounter.getTickProgress(true)));
        initialized = true;
    }

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        RenderState state = RenderState.create(mc, tickDelta);
        
        EspMod esp = getModule(EspMod.class);
        if (esp != null && esp.isEnabled()) {
            renderLivingEntities(ctx, mc, state, esp);
        }

        ItemEspMod itemEsp = getModule(ItemEspMod.class);
        if (itemEsp != null && itemEsp.isEnabled()) {
            renderItems(ctx, mc, state, itemEsp);
        }

        ChestEspMod chestEsp = getModule(ChestEspMod.class);
        if (chestEsp != null && chestEsp.isEnabled()) {
            renderChests(ctx, mc, state);
        }
    }

    private static void renderLivingEntities(DrawContext ctx, MinecraftClient mc, RenderState state, EspMod esp) {
        double rangeSq = esp.getRange() * esp.getRange();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player || !living.isAlive()) continue;
            if (mc.player.squaredDistanceTo(living) > rangeSq) continue;

            boolean isPlayer = living instanceof PlayerEntity;
            if (isPlayer && !esp.shouldRenderPlayers()) continue;
            if (!isPlayer && !esp.shouldRenderMobs()) continue;

            ScreenBox box = projectBox(living.getBoundingBox(), state);
            if (box == null) continue;

            int color = espColor(esp, isPlayer, System.currentTimeMillis());
            if (esp.shouldFillBoxes()) {
                ctx.fill(box.x, box.y, box.x + box.w, box.y + box.h, (color & 0x00FFFFFF) | 0x30000000);
            }

            drawOutlinedBox(ctx, box.x, box.y, box.w, box.h, color);

            if (esp.shouldShowNames()) {
                drawCenteredName(ctx, mc, living.getName().getString(), box, color);
            }

            if (esp.shouldShowHealthBar()) {
                drawHealthBar(ctx, living, box);
            }
        }
    }

    private static void renderItems(DrawContext ctx, MinecraftClient mc, RenderState state, ItemEspMod itemEsp) {
        double rangeSq = itemEsp.getRange() * itemEsp.getRange();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity item)) continue;
            if (!item.isAlive() || mc.player.squaredDistanceTo(item) > rangeSq) continue;

            ScreenBox box = projectBox(item.getBoundingBox(), state);
            if (box == null) continue;

            int color = 0xFFFFCC44;
            drawOutlinedBox(ctx, box.x, box.y, box.w, box.h, color);

            if (itemEsp.shouldShowNames()) {
                drawCenteredName(ctx, mc, itemName(item), box, color);
            }
        }
    }

    private static void renderChests(DrawContext ctx, MinecraftClient mc, RenderState state) {
        if (mc.world == null) return;
        com.example.liquorclient.mixin.ClientWorldAccessor worldAccessor = (com.example.liquorclient.mixin.ClientWorldAccessor) mc.world;
        for (net.minecraft.block.entity.BlockEntity be : worldAccessor.liquor$getBlockEntities()) {
            int color = 0;
            if (be instanceof net.minecraft.block.entity.ChestBlockEntity) {
                color = 0xFFFFA500; // Orange
            } else if (be instanceof net.minecraft.block.entity.EnderChestBlockEntity) {
                color = 0xFFA020F0; // Purple
            } else if (be instanceof net.minecraft.block.entity.ShulkerBoxBlockEntity) {
                color = 0xFFFF69B4; // Pink
            } else if (be instanceof net.minecraft.block.entity.TrappedChestBlockEntity) {
                color = 0xFFFF0000; // Red
            }

            if (color != 0) {
                ScreenBox box = projectBox(new Box(be.getPos()), state);
                if (box != null) {
                    drawOutlinedBox(ctx, box.x, box.y, box.w, box.h, color);
                }
            }
        }
    }

    private static String itemName(ItemEntity item) {
        String name = item.getStack().getName().getString();
        int count = item.getStack().getCount();
        return count > 1 ? name + " " + count + "x" : name;
    }

    private static int espColor(EspMod esp, boolean isPlayer, long now) {
        if (esp.shouldUseRainbow()) {
            return rainbowColor(now);
        }
        return isPlayer ? 0xFFFF4444 : 0xFF4488FF;
    }

    private static int rainbowColor(long now) {
        float hue = (now % 2200L) / 2200.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.9f, 1.0f) | 0xFF000000;
    }

    private static ScreenBox projectBox(Box box, RenderState state) {
        float minX = (float) (box.minX - state.cameraPos.x);
        float minY = (float) (box.minY - state.cameraPos.y);
        float minZ = (float) (box.minZ - state.cameraPos.z);
        float maxX = (float) (box.maxX - state.cameraPos.x);
        float maxY = (float) (box.maxY - state.cameraPos.y);
        float maxZ = (float) (box.maxZ - state.cameraPos.z);

        float[][] corners = {
                {minX, minY, minZ}, {maxX, minY, minZ},
                {minX, maxY, minZ}, {maxX, maxY, minZ},
                {minX, minY, maxZ}, {maxX, minY, maxZ},
                {minX, maxY, maxZ}, {maxX, maxY, maxZ}
        };

        float sMinX = Float.MAX_VALUE;
        float sMinY = Float.MAX_VALUE;
        float sMaxX = -Float.MAX_VALUE;
        float sMaxY = -Float.MAX_VALUE;
        boolean visible = false;

        for (float[] corner : corners) {
            Vector4f pos = new Vector4f(corner[0], corner[1], corner[2], 1.0f);
            pos.mul(state.view).mul(state.projection);

            if (pos.w <= 0.01f) continue;
            visible = true;

            float sx = (pos.x / pos.w * 0.5f + 0.5f) * state.screenW;
            float sy = (1.0f - (pos.y / pos.w * 0.5f + 0.5f)) * state.screenH;

            sMinX = Math.min(sMinX, sx);
            sMinY = Math.min(sMinY, sy);
            sMaxX = Math.max(sMaxX, sx);
            sMaxY = Math.max(sMaxY, sy);
        }

        if (!visible) return null;

        int x1 = Math.max(0, Math.round(sMinX));
        int y1 = Math.max(0, Math.round(sMinY));
        int x2 = Math.min(state.screenW, Math.round(sMaxX));
        int y2 = Math.min(state.screenH, Math.round(sMaxY));
        if (x2 <= x1 || y2 <= y1) return null;

        return new ScreenBox(x1, y1, x2 - x1, y2 - y1);
    }

    private static void drawOutlinedBox(DrawContext ctx, int x, int y, int w, int h, int color) {
        drawOutline(ctx, x - 1, y - 1, w + 2, h + 2, 0x90000000);
        drawOutline(ctx, x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2), 0x90000000);
        drawOutline(ctx, x, y, w, h, color);
    }

    private static void drawOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private static void drawCenteredName(DrawContext ctx, MinecraftClient mc, String name, ScreenBox box, int color) {
        int nameW = mc.textRenderer.getWidth(name);
        ctx.drawTextWithShadow(mc.textRenderer, name, box.x + box.w / 2 - nameW / 2, box.y - 10, color);
    }

    private static void drawHealthBar(DrawContext ctx, LivingEntity living, ScreenBox box) {
        float maxHealth = Math.max(1.0f, living.getMaxHealth());
        float pct = Math.max(0.0f, Math.min(1.0f, living.getHealth() / maxHealth));
        int barX = box.x - 5;

        ctx.fill(barX - 1, box.y - 1, barX + 3, box.y + box.h + 1, 0x90000000);
        int fillY = box.y + Math.round(box.h * (1.0f - pct));
        ctx.fill(barX, fillY, barX + 2, box.y + box.h, healthColor(pct));
    }

    private static int healthColor(float pct) {
        if (pct > 0.5f) return 0xFF00FF00;
        if (pct > 0.25f) return 0xFFFFAA00;
        return 0xFFFF0000;
    }

    private static <T extends Module> T getModule(Class<T> type) {
        for (Module module : ModuleManager.getModules()) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        return null;
    }

    private record RenderState(
            int screenW,
            int screenH,
            Vec3d cameraPos,
            Matrix4f projection,
            Matrix4f view
    ) {
        static RenderState create(MinecraftClient mc, float tickDelta) {
            int screenW = mc.getWindow().getScaledWidth();
            int screenH = mc.getWindow().getScaledHeight();
            float aspect = (float) screenW / Math.max(1, screenH);
            float fov = mc.options.getFov().getValue().floatValue();

            Camera camera = mc.gameRenderer.getCamera();
            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(fov), aspect, 0.05f, 1000.0f);
            Matrix4f view = new Matrix4f().rotation(camera.getRotation()).invert();

            Vec3d cameraPos = mc.player.getLerpedPos(tickDelta).add(0.0, mc.player.getStandingEyeHeight(), 0.0);
            return new RenderState(screenW, screenH, cameraPos, projection, view);
        }
    }

    private record ScreenBox(int x, int y, int w, int h) {
    }
}
