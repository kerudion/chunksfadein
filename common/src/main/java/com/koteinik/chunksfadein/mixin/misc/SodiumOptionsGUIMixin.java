package com.koteinik.chunksfadein.mixin.misc;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.sodium.CFISodiumPage;

import net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.minecraft.client.gui.screens.Screen;

@Mixin(SodiumOptionsGUI.class)
public class SodiumOptionsGUIMixin {
	@Shadow
	private List<OptionPage> pages;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void modifyInit(Screen prevScreen, CallbackInfo ci) {
		if (Config.showModTabInSettings)
			pages.add(new CFISodiumPage());
	}
}
