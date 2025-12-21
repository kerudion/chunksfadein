package com.koteinik.chunksfadein.compat.dh.mixin;

import com.koteinik.chunksfadein.compat.dh.DHState;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.render.LodRenderSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LodRenderSection.class, remap = false)
public abstract class LodRenderSectionMixin {
	@Shadow
	@Final
	public long pos;

	@Inject(method = "uploadToGpuAsync", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/dataObjects/render/bufferBuilding/ColumnRenderBufferBuilder;uploadBuffersAsync(Lcom/seibel/distanthorizons/core/level/IDhClientLevel;JLcom/seibel/distanthorizons/core/dataObjects/render/bufferBuilding/LodQuadBuilder;)Ljava/util/concurrent/CompletableFuture;"))
	private void modifyUploadToGpuAsync(LodQuadBuilder lodQuadBuilder, CallbackInfo ci) {
		DHState.sectionPosForCreatingBuffer.set(pos);
	}
}
