package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.compat.sodium.ext.CommandListExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.config.Config;
import com.llamalad7.mixinextras.sugar.Local;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.gl.device.MultiDrawBatch;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.embeddedt.embeddium.impl.render.chunk.DefaultChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.data.SectionRenderDataStorage;
import org.embeddedt.embeddium.impl.render.chunk.lists.ChunkRenderList;
import org.embeddedt.embeddium.impl.render.chunk.lists.ChunkRenderListIterable;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class DefaultChunkRendererMixin {
	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/DefaultChunkRenderer;executeDrawBatch(Lorg/embeddedt/embeddium/impl/gl/device/CommandList;Lorg/embeddedt/embeddium/impl/gl/tessellation/GlTessellation;Lorg/embeddedt/embeddium/impl/gl/device/MultiDrawBatch;)V"
		)
	)
	private void modifyChunkRender(
		ChunkRenderMatrices matrices,
		CommandList commandList,
		ChunkRenderListIterable renderLists,
		TerrainRenderPass renderPass,
		CameraTransform camera,
		CallbackInfo ci,
		@Local(ordinal = 0) ChunkShaderInterface shader,
		@Local(ordinal = 0) RenderRegion region
	) {
		if (shader == null)
			return;
		if (!Config.isModEnabled)
			return;

		// Made to not interrupt Axiom mixin
		uploadToBuffer(commandList, shader, region);
	}

	@Inject(
		method = "fillCommandBuffer",
		at = @At(
			value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/data/SectionRenderDataUnsafe;getSliceMask(J)I"
		)
	)
	private static void modifyFillCommandBuffer(MultiDrawBatch batch,
	                                            RenderRegion region,
	                                            SectionRenderDataStorage renderDataStorage,
	                                            ChunkRenderList renderList,
	                                            CameraTransform camera,
	                                            TerrainRenderPass pass,
	                                            boolean useBlockFaceCulling,
	                                            CallbackInfo ci,
	                                            @Local(name = "sectionIndex") int sectionIndex) {
		// Made to not interrupt Axiom mixin
		if (Config.isModEnabled)
			processChunk(region, sectionIndex);
	}

	private void uploadToBuffer(CommandList commandList, ChunkShaderInterface shader, RenderRegion region) {
		ChunkShaderInterfaceExt ext = (ChunkShaderInterfaceExt) shader;
		RenderRegionExt regionExt = (RenderRegionExt) region;

		regionExt.uploadToBuffer(ext, (CommandListExt) commandList);
	}

	private static void processChunk(RenderRegion region, int sectionIndex) {
		RenderSection section = region.getSection(sectionIndex);
		if (section == null) return;

		RenderRegionExt regionExt = (RenderRegionExt) region;

		regionExt.processChunk((RenderSectionExt) section, sectionIndex);
	}
}
