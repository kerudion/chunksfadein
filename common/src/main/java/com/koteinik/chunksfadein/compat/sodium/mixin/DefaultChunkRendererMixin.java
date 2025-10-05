package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.config.Config;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
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
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/DefaultChunkRenderer;executeDrawBatch(Lnet/caffeinemc/mods/sodium/client/gl/device/CommandList;Lnet/caffeinemc/mods/sodium/client/gl/tessellation/GlTessellation;Lnet/caffeinemc/mods/sodium/client/gl/device/MultiDrawBatch;)V"
		)
	)
	private void modifyChunkRender(
		ChunkRenderMatrices matrices,
		CommandList commandList,
		ChunkRenderListIterable renderLists,
		TerrainRenderPass pass,
		CameraTransform camera,
		FogParameters parameters,
		boolean indexedRenderingEnabled,
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

	private void uploadToBuffer(CommandList commandList, ChunkShaderInterface shader, RenderRegion region) {
		ChunkShaderInterfaceExt ext = (ChunkShaderInterfaceExt) shader;
		RenderRegionExt regionExt = (RenderRegionExt) region;

		regionExt.uploadToBuffer(ext, commandList);
	}
}
