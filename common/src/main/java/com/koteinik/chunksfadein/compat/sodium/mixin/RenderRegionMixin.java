package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExt {
	@Shadow
	@Final
	private RenderSection[] sections;

	@Shadow
	private int uniqueId;

	@Override
	public RenderSectionExt getSection(int sectionIndex) {
		return (RenderSectionExt) sections[sectionIndex];
	}

	@Inject(method = "delete", at = @At(value = "TAIL"))
	private void modifyDeleteResources(CallbackInfo ci) {
		if (uniqueId == -1) return;

		SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
		if (renderer == null)
			return;

		ChunkFadeInController controller = ((SodiumWorldRendererExt) renderer).getChunkFadeInController();
		if (controller == null)
			return;

		controller.cleanRegion(uniqueId);
	}
}
