package com.example.chunkfinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChunkFinderClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static KeyBinding radiusKey;
    private static boolean enabled = false;
    private static int radiusChunks = 2;
    private static int sensitivity = 1; // 0 = strict, 1 = normal, 2 = broad
    private static int ticksUntilScan = 0;
    private static final Map<BlockPos, String> FOUND = new LinkedHashMap<>();
    private static final int MAX_RESULTS = 8;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chunkfinder.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC));
        radiusKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chunkfinder.radius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H,
                KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Chunk Finder: " + (enabled ? "ON" : "OFF")), true);
                }
            }
            while (radiusKey.wasPressed()) {
                radiusChunks++;
                if (radiusChunks > 4) radiusChunks = 1;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Chunk Finder radius: " + radiusChunks + " chunks"), true);
                }
            }
            if (!enabled || client.world == null || client.player == null) return;
            if (ticksUntilScan-- <= 0) {
                scan(client);
                ticksUntilScan = 100; // scan every five seconds
            }
        });

        HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || !enabled) return;
            int x = 8, y = 8;
            context.drawText(client.textRenderer, Text.literal("Chunk Finder: ON | Radius: " + radiusChunks + " chunks"), x, y, 0xFFB86BFF, true);
            y += 12;
            context.drawText(client.textRenderer, Text.literal("Sensitivity: " + sensitivityName()), x, y, 0xFFFFFFFF, true);
            y += 12;
            int shown = 0;
            for (String line : FOUND.values()) {
                if (shown++ >= MAX_RESULTS) break;
                context.drawText(client.textRenderer, Text.literal(line), x, y, 0xFFB7F7D4, true);
                y += 11;
            }
            if (FOUND.isEmpty()) {
                context.drawText(client.textRenderer, Text.literal("No amethyst indicators found in scanned area."), x, y, 0xFFCCCCCC, true);
            }
        });
    }

    private static String sensitivityName() {
        return switch (sensitivity) {
            case 0 -> "Strict";
            case 2 -> "Broad";
            default -> "Normal";
        };
    }

    private static void scan(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        BlockPos playerPos = client.player.getBlockPos();
        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int minY = Math.max(client.world.getBottomY(), playerPos.getY() - 24);
        int maxY = Math.min(client.world.getTopYInclusive(), playerPos.getY() + 24);
        Map<BlockPos, String> fresh = new LinkedHashMap<>();

        for (int cx = centerChunkX - radiusChunks; cx <= centerChunkX + radiusChunks; cx++) {
            for (int cz = centerChunkZ - radiusChunks; cz <= centerChunkZ + radiusChunks; cz++) {
                WorldChunk chunk = client.world.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue; // never request an unloaded chunk
                int startX = cx << 4, startZ = cz << 4;
                for (int x = startX; x < startX + 16; x++) {
                    for (int z = startZ; z < startZ + 16; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            Block block = chunk.getBlockState(new BlockPos(x, y, z)).getBlock();
                            String label = classify(block);
                            if (label != null) {
                                BlockPos pos = new BlockPos(x, y, z).toImmutable();
                                fresh.put(pos, label + " at " + x + " " + y + " " + z);
                                if (fresh.size() >= 30) break;
                            }
                        }
                        if (fresh.size() >= 30) break;
                    }
                    if (fresh.size() >= 30) break;
                }
            }
        }
        FOUND.clear();
        FOUND.putAll(fresh);
    }

    private static String classify(Block block) {
        if (block == Blocks.BUDDING_AMETHYST) return "Budding amethyst";
        if (block == Blocks.AMETHYST_BLOCK) return sensitivity >= 1 ? "Amethyst block" : null;
        if (block == Blocks.AMETHYST_CLUSTER || block == Blocks.LARGE_AMETHYST_BUD
                || block == Blocks.MEDIUM_AMETHYST_BUD || block == Blocks.SMALL_AMETHYST_BUD) {
            return sensitivity >= 1 ? "Amethyst growth" : null;
        }
        if (sensitivity >= 2 && (block == Blocks.CALCITE || block == Blocks.SMOOTH_BASALT)) {
            return "Possible geode material";
        }
        return null;
    }
}
