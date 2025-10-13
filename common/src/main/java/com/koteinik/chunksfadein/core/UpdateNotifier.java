package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.core.ModrinthApi.ModrinthVersion;
import com.koteinik.chunksfadein.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class UpdateNotifier {
	public static void checkAndNotify() {
		new Thread(() -> {
			try {
				List<ModrinthVersion> newVersions = ModrinthApi.getLatestVersions().stream()
					.takeWhile(UpdateNotifier::isNewerVersion)
					.toList();

				if (!newVersions.isEmpty()) {
					ModrinthVersion latestVersion = newVersions.getFirst();

					List<Component> textList = new ArrayList<>();
					textList.add(Component.literal("§7New version of §2Chunks Fade In §7is available!"));

					Style linkStyle = Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(latestVersion.downloadUrl)));

					String versions = newVersions.size() == 1
						? "§6v" + latestVersion.version
						: "§6v%s§7-§6v%s".formatted(newVersions.getLast().version, latestVersion.version);
					textList.add(Component.literal(versions + "§r§7 changelog:"));

					String changelogs = String.join(
						"\n", newVersions.stream()
							.map(v -> v.changelog)
							.toList()
					).replace("\r", "");
					textList.add(Component.literal("§7" + changelogs));

					textList.addAll(Component.literal("§7§nClick to download").toFlatList(linkStyle));

					Minecraft minecraft = Minecraft.getInstance();
					LocalPlayer player = minecraft.player;

					if (player == null)
						return;

					for (Component text : textList)
						Minecraft.getInstance().execute(() -> player.displayClientMessage(text, false));
				}
			} catch (Exception e) {
				Logger.warn("Failed to get latest mod version!");
				e.printStackTrace();
			}
		}).start();
	}

	private static boolean isNewerVersion(ModrinthVersion modrinthVersion) {
		if (modrinthVersion == null)
			return false;

		return Services.PLATFORM.getModVersion().compareTo(modrinthVersion.version) < 0;
	}
}
