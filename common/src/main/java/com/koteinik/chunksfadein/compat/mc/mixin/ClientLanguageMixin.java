package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.crowdin.Translations;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ClientLanguage.class)
public class ClientLanguageMixin {
	@Inject(
		method = "loadFrom",
		at = @At(
			value = "RETURN"
		)
	)
	private static void cfi_captureOverrides(
		ResourceManager resourceManager,
		List<String> filenames,
		boolean defaultRightToLeft,
		CallbackInfoReturnable<ClientLanguage> cir
	) {
		Translations.setPackOverrides(resourceManager, filenames);
	}
}
