package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.compat.mc.Keybinds;
import com.koteinik.chunksfadein.core.ModrinthApi;
import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.List;

@Mod(value = "chunksfadein", dist = Dist.CLIENT)
public class ChunksFadeIn {
	public static List<KeyMapping> KEYLIST = new ArrayList<>();

	public ChunksFadeIn(IEventBus bus, ModContainer container) {
		bus.addListener(this::registerKeys);

		Config.load();
		ModrinthApi.load();
		Translations.download();

		container.registerExtensionPoint(
			IConfigScreenFactory.class,
			(modContainer, modListScreen) -> new SettingsScreen(modListScreen)
		);
	}

	public void registerKeys(RegisterKeyMappingsEvent event) {
		Keybinds.initKeybinds();
		KEYLIST.forEach(event::register);
		KEYLIST.clear();
	}
}
