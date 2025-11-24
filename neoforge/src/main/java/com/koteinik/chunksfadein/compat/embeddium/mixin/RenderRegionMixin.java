package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.compat.sodium.ext.CommandListExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import org.embeddedt.embeddium.impl.gl.arena.staging.StagingBuffer;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.gl.device.GLRenderDevice;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExt {
	private ChunkFadeInController fadeController;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void modifyConstructor(int x, int y, int z, StagingBuffer stagingBuffer, CallbackInfo ci) {
		fadeController = new ChunkFadeInController((CommandListExt) GLRenderDevice.INSTANCE.createCommandList());
	}

	@Inject(method = "delete", at = @At(value = "TAIL"))
	private void modifyDeleteResources(CommandList commandList, CallbackInfo ci) {
		fadeController.delete((CommandListExt) commandList);
	}

	@Override
	public void processChunk(RenderSectionExt section, int sectionIndex) {
		fadeController.processChunk(section, sectionIndex);
	}

	@Override
	public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandListExt commandList) {
		fadeController.uploadToBuffer(shader, commandList);
	}
}
