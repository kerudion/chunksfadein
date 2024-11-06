package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.platform.Services;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class Keybinds {
	private static final String KEYBINDS = "chunksfadein.keybinds";
	private static final String TOGGLE_MOD = "chunksfadein.keybinds.toggleMod";

	private static final Component FADING_ENABLED = Component.translatable("chunksfadein.alerts.fading.enabled");
	private static final Component FADING_DISABLED = Component.translatable("chunksfadein.alerts.fading.disabled");

	public static KeyMapping toggleModKeybind = null;

	public static void initKeybinds() {
		toggleModKeybind = Services.PLATFORM.registerKeyBind(new KeyMapping(TOGGLE_MOD, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEYBINDS));
	}

	public static void handleKeybinds(Minecraft minecraft) {
		if (toggleModKeybind != null && toggleModKeybind.consumeClick()) {
			boolean enabled = Config.flipBoolean(Config.MOD_ENABLED_KEY);

			ShaderUtils.reloadWorldRenderer();

			LocalPlayer player = minecraft.player;
			if (player != null)
				player.displayClientMessage(enabled ? FADING_ENABLED : FADING_DISABLED, false);
		}
	}
}
