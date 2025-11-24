package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.DataBuffer;
import com.koteinik.chunksfadein.core.Fader;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import net.minecraft.core.SectionPos;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin implements RenderSectionExt {
	@Shadow
	@Final
	private int chunkX;

	@Shadow
	@Final
	private int chunkY;

	@Shadow
	@Final
	private int chunkZ;

	private Fader fader;
	private boolean completedFade = false;
	private boolean completedAnimation = false;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void modifyInit(RenderRegion region, int chunkX, int chunkY, int chunkZ, CallbackInfo ci) {
		fader = new Fader(chunkX, chunkZ);
	}

	@Override
	public boolean hasRenderedBefore() {
		return fader.hasRenderedBefore();
	}

	@Override
	public void setRenderedBefore() {
		fader.setRenderedBefore();
	}

	@Override
	public void dhMarkRendered() {
		if (completedFade)
			LodMaskTexture.markRendered(chunkX, chunkY, chunkZ);
	}

	@Override
	public boolean incrementFadeCoeff(long delta, int sectionIndex, DataBuffer buffer) {
		if (completedFade)
			return false;

		float fadeCoeff = fader.incrementFadeCoeff(delta, isNearPlayer());
		buffer.put(sectionIndex, 3, fadeCoeff);

		completedFade |= fadeCoeff == 1f;
		return true;
	}

	@Override
	public boolean incrementAnimationOffset(long delta, int sectionIndex, DataBuffer buffer) {
		if (completedAnimation)
			return false;

		if (!Config.animateWithDH && CompatibilityHook.isDHRenderingEnabled())
			return completedAnimation = true;

		float[] offset = fader.incrementAnimationOffset(delta, isNearPlayer());
		for (int i = 0; i < 3; i++)
			buffer.put(sectionIndex, i, offset[i]);

		completedAnimation |= offset[0] == 0f && offset[1] == 0f && offset[2] == 0f;
		return true;
	}

	@Override
	public long calculateAndGetDelta() {
		return fader.calculateAndGetDelta();
	}

	@Override
	public float[] getAnimationOffset() {
		return fader.getAnimationOffset();
	}

	@Override
	public float getFadeCoeff() {
		return fader.getFadeCoeff();
	}

	private boolean isNearPlayer() {
		SectionPos chunkPos = SectionPos.of(Utils.cameraPosition());

		final int camChunkX = chunkPos.getX();
		final int camChunkZ = chunkPos.getZ();

		return MathUtils.chunkInRange(chunkX, chunkZ, camChunkX, camChunkZ, 1);
	}
}
