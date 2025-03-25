package com.koteinik.chunksfadein.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.koteinik.chunksfadein.core.ModrinthApi.ModrinthVersion;
import com.koteinik.chunksfadein.platform.Services;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class UpdateNotifier {
	public static void checkAndNotify() {
		new Thread(() -> {
			ModrinthVersion latestVersion = ModrinthApi.getLatestModVersion();

			if (isNewerVersion(latestVersion)) {
				List<Component> textList = new ArrayList<>();
				textList.add(Component.literal("§7New version of §2Chunks fade in §7is available!"));

				Style linkStyle = Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(latestVersion.downloadUrl)));

				textList.add(Component.literal("§7v" + latestVersion.version + "§r§7 changelog:"));
				textList.add(Component.literal("§7" + latestVersion.changelog));
				textList.addAll(Component.literal("§7§nClick to download").toFlatList(linkStyle));

				Minecraft minecraft = Minecraft.getInstance();
				LocalPlayer player = minecraft.player;

				if (player == null)
					return;

				for (Component text : textList)
					player.displayClientMessage(text, false);
			}
		}).start();
	}

	private static boolean isNewerVersion(ModrinthVersion modrinthVersion) {
		if (modrinthVersion == null)
			return false;

		return Services.PLATFORM.getModVersion().compareTo(modrinthVersion.version) < 0;
	}
}
