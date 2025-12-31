package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.crowdin.Translations;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Language.class)
public class LanguageMixin {
	@Inject(
		method = "getOrDefault(Ljava/lang/String;)Ljava/lang/String;",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void useTranslations(String id, CallbackInfoReturnable<String> cir) {
		if (!Translations.hasKey(id)) return;

		cir.setReturnValue(Translations.resolve(id));
		cir.cancel();
	}
}
