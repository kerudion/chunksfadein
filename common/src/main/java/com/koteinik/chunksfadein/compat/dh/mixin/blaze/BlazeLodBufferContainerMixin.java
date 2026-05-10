package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.compat.dh.DHState;
import com.koteinik.chunksfadein.compat.dh.ext.BlazeLodBufferContainerExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Fader;
import com.koteinik.chunksfadein.core.Utils;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import com.seibel.distanthorizons.core.pos.blockPos.DhBlockPos;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.koteinik.chunksfadein.compat.dh.DHBlazeState.chunkFadeData;

@Mixin(value = LodBufferContainer.class, remap = false)
public abstract class BlazeLodBufferContainerMixin implements BlazeLodBufferContainerExt {
	@Shadow
	@Final
	public DhBlockPos minCornerBlockPos;

	@Shadow
	@Final
	public long pos;

	@Unique
	private Fader cfi_fader = null;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void cfi_init(long pos, DhBlockPos minCornerBlockPos, CallbackInfo ci) {
		cfi_fader = DHState.getFader(pos);
	}

	@Override
	public void cfi_upload() {
		if (!Config.isModEnabled) return;
		if (cfi_fader == null) return;

		long delta = cfi_fader.calculateAndGetDelta();

		boolean inRenderDistance = cfi_isInRenderDistance();
		float[] xyz = cfi_fader.incrementAnimationOffset(delta, inRenderDistance);
		float x = xyz[0];
		float y = xyz[1];
		float z = xyz[2];
		float w = cfi_fader.incrementFadeCoeff(delta, inRenderDistance);
		cfi_fader.setRenderedBefore();

		if (chunkFadeData != -1)
			GL30.glUniform4f(chunkFadeData, x, y, z, w);
	}

	@Unique
	private boolean cfi_isInRenderDistance() {
		int size = DhSectionPos.getChunkWidth(pos);
		int bX = (int) Math.floor((double) minCornerBlockPos.getX() / 16);
		int bZ = (int) Math.floor((double) minCornerBlockPos.getZ() / 16);

		Vec3 cameraPosition = Utils.cameraPosition();
		int cX = (int) Math.floor(cameraPosition.x / 16);
		int cZ = (int) Math.floor(cameraPosition.z / 16);

		int renderDistance = Utils.chunkRenderDistance();

		if (bX < cX) bX += size;
		if (bZ < cZ) bZ += size;

		return Math.abs(cX - bX) <= renderDistance
			&& Math.abs(cZ - bZ) <= renderDistance;
	}
}
