package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
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
			target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBuffer;)V",
			shift = At.Shift.AFTER,
			ordinal = 1
		)
	)
	private void cfi_bindUniforms(
		ChunkRenderMatrices matrices,
		ChunkRenderListIterable renderLists,
		TerrainRenderPass renderPass,
		CameraTransform camera,
		FogParameters parameters,
		boolean indexedRenderingEnabled,
		GpuSampler terrainSampler,
		GpuBuffer uniformData,
		GpuBuffer sectionTimeInfo,
		CallbackInfo ci,
		@Local(name = "pass") RenderPass pass
	) {
		SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
		if (renderer == null)
			return;

		ChunkFadeInController controller = ((SodiumWorldRendererExt) renderer).getChunkFadeInController();
		if (controller == null)
			return;

		pass.setUniform("cfi_u_FadeData", controller.getFadeBuffer());
		pass.setUniform("cfi_u_Globals", controller.getUniformBuffer());
		pass.bindTexture(
			"cfi_sky",
			SkyFBO.getInstance().texture.getColorTextureView(),
			RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
		);
	}
}
