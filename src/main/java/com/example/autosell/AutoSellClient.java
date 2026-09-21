package com.example.autosell;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class AutoSellClient implements ClientModInitializer {

    // The command WITHOUT the leading slash
    private static final String COMMAND = "sellall inventory";

    // 20 ticks = 1 second, so 1200 ticks = 1 minute
    private static final int INTERVAL_TICKS = 20 * 60;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("autosell", "main"));

    private static KeyMapping toggleKey;
    private boolean enabled = false;
    private int ticksLeft = 0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.autosell.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft client) {
        // Toggle on/off with the keybind
        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            ticksLeft = 0; // run immediately when turned on
            if (client.player != null) {
                client.player.displayClientMessage(
                        Component.literal("AutoSell: " + (enabled ? "ON" : "OFF")), true);
            }
        }

        if (!enabled) return;

        // Auto-disable if we left the world / server
        if (client.player == null || client.getConnection() == null) {
            enabled = false;
            return;
        }

        if (ticksLeft <= 0) {
            client.getConnection().sendCommand(COMMAND);
            ticksLeft = INTERVAL_TICKS;
        } else {
            ticksLeft--;
        }
    }
}
