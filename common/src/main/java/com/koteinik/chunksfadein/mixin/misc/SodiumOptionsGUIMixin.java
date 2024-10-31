package com.koteinik.chunksfadein.mixin.misc;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableList;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.SettingsScreen;

import net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(SodiumOptionsGUI.class)
public class SodiumOptionsGUIMixin extends Screen {
	private static final String SODIUM_PAGE_NAME = "settings.chunksfadein.sodium_page_name";

	@Shadow(remap = false)
	@Final
	private List<OptionPage> pages;

	@Unique
	private OptionPage shaderPacks = null;

	protected SodiumOptionsGUIMixin(Component title) {
		super(title);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void modifyInit(Screen prevScreen, CallbackInfo ci) {
		if (Config.showModTabInSettings) {
			shaderPacks = new OptionPage(Component.translatable(SODIUM_PAGE_NAME), ImmutableList.of());
			pages.add(shaderPacks);
		}
	}

	@Inject(method = "setPage", at = @At("HEAD"), remap = false, cancellable = true)
	private void modifySetPage(OptionPage page, CallbackInfo ci) {
		if (page != shaderPacks)
			return;

		minecraft.setScreen(new SettingsScreen(this));
		ci.cancel();
	}
}
