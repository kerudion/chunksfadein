package com.koteinik.chunksfadein.compat.dh.mixin.iris;

import com.koteinik.chunksfadein.compat.dh.DHState;
import com.koteinik.chunksfadein.compat.dh.ext.DhRenderProgramExt;
import com.koteinik.chunksfadein.compat.dh.ext.LodBufferContainerExt;
import com.koteinik.chunksfadein.compat.dh.ext.LodRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Fader;
import com.koteinik.chunksfadein.core.Utils;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import com.seibel.distanthorizons.core.pos.blockPos.DhBlockPos;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.compat.dh.DHCompatInternal;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LodBufferContainer.class, remap = false)
public abstract class IrisLodBufferContainerMixin implements LodBufferContainerExt {
	@Shadow
	@Final
	public DhBlockPos minCornerBlockPos;

	private Fader fader = null;
	private long sectionPos = 0L;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void modifyConstructor(long pos, DhBlockPos minCornerBlockPos, CallbackInfo ci) {
		sectionPos = DHState.sectionPosForCreatingBuffer.get();
		DHState.sectionPosForCreatingBuffer.remove();
		fader = DHState.getFader(sectionPos);
	}

	@Override
	public void bind(LodRenderer renderContext) {
		if (!Config.isModEnabled) return;
		if (!(renderContext instanceof LodRendererExt rendererExt)) return;
		if (fader == null) return;

		long delta = fader.calculateAndGetDelta();

		boolean inRenderDistance = isInRenderDistance();
		float[] xyz = fader.incrementAnimationOffset(delta, inRenderDistance);
		float x = xyz[0];
		float y = xyz[1];
		float z = xyz[2];
		float w = fader.incrementFadeCoeff(delta, inRenderDistance);
		fader.setRenderedBefore();

		DHCompatInternal irisDh = (DHCompatInternal) Iris.getPipelineManager()
			.getPipeline()
			.map(WorldRenderingPipeline::getDHCompat)
			.map(DHCompat::getInstance)
			.orElse(null);
		if (irisDh != null) {
			if (irisDh.getSolidShader() instanceof DhRenderProgramExt ext)
				ext.bindUniforms(x, y, z, w);
			if (irisDh.getShadowShader() instanceof DhRenderProgramExt ext)
				ext.bindUniforms(x, y, z, w);
			if (irisDh.getTranslucentShader() instanceof DhRenderProgramExt ext)
				ext.bindUniforms(x, y, z, w);

			return;
		}

		DhRenderProgramExt shader = rendererExt.getShader();
		if (shader == null) return;

		shader.bindUniforms(x, y, z, w);
	}

	private boolean isInRenderDistance() {
		int size = DhSectionPos.getChunkWidth(sectionPos);
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
