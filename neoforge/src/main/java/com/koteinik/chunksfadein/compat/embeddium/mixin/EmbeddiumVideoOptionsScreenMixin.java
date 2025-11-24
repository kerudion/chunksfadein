package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.embeddium.CFIEmbeddiumPage;
import org.embeddedt.embeddium.api.options.structure.OptionPage;
import org.embeddedt.embeddium.impl.gui.EmbeddiumVideoOptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmbeddiumVideoOptionsScreen.class, remap = false)
public class EmbeddiumVideoOptionsScreenMixin {
	@Shadow
	@Final
	private List<OptionPage> pages;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void addChunksFadeInPage(CallbackInfo ci) {
		pages.add(new CFIEmbeddiumPage());
	}
}
