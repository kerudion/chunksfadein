package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.compat.mc.Keybinds;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ModrinthApi;
import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;

@Mod(value = "chunksfadein")
public class ChunksFadeIn {
	public static List<KeyMapping> KEYLIST = new ArrayList<>();

	public ChunksFadeIn() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ChunksFadeIn::init);
	}

	public static void init() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(ChunksFadeIn::registerKeys);

		Config.load();
		ModrinthApi.load();
		Translations.download();

		ModLoadingContext.get().registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory(
				(minecraft, modListScreen) -> new SettingsScreen(modListScreen)
			)
		);
	}

	public static void registerKeys(RegisterKeyMappingsEvent event) {
		Keybinds.initKeybinds();
		KEYLIST.forEach(event::register);
		KEYLIST.clear();
	}
}
