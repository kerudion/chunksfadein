package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionManagerExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Utils;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExt {
	@Shadow
	private RenderSectionManager renderSectionManager;
	@Shadow
	private ClientLevel level;
	@Shadow
	private int renderDistance;

	@Unique
	private ChunkFadeInController cfi_chunkFadeInController = null;

	@Inject(method = "initRenderer", at = @At("TAIL"))
	private void cfi_initController(CallbackInfo ci) {
		cfi_chunkFadeInController = new ChunkFadeInController(level, renderDistance);
	}

	@Inject(method = "deleteRendererState", at = @At("TAIL"))
	private void cfi_deleteController(CallbackInfo ci) {
		if (cfi_chunkFadeInController != null) {
			cfi_chunkFadeInController.delete();
			cfi_chunkFadeInController = null;
		}
	}

	@Override
	public float[] getAnimationOffset(Vec3 pos) {
		SectionPos chunkPos = SectionPos.of(pos);
		float[] offset = ((RenderSectionManagerExt) renderSectionManager).getAnimationOffset(
			chunkPos.getX(),
			chunkPos.getY(),
			chunkPos.getZ()
		);

		if (Config.isCurvatureEnabled) {
			Vec3 cam = Utils.cameraPosition();

			double x = pos.x - cam.x;
			double z = pos.z - cam.z;

			if (offset == null)
				offset = new float[3];
			else
				offset = offset.clone();

			offset[1] -= (float) ((x * x + z * z) / Config.worldCurvature);
		}

		return offset;
	}

	@Override
	public @Nullable RenderSectionManager getRenderSectionManager() {
		return renderSectionManager;
	}

	@Override
	public @Nullable ChunkFadeInController getChunkFadeInController() {
		return cfi_chunkFadeInController;
	}
}
