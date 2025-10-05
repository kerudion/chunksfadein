package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(value = LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Final
	private LevelTargetBundle targets;

	@Inject(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;)V"
		)
	)
	private void modifyRenderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
		if (!Config.isModEnabled || (!Config.isFadeEnabled && !Config.isAnimationEnabled))
			return;

		if (Config.isFadeEnabled) {
			FramePass framePass = frameGraphBuilder.addPass("cfi_blit_sky");
			targets.main = framePass.readsAndWrites(targets.main);

			framePass.executes(() -> {
				SkyFBO fbo = SkyFBO.getInstance();
				if (fbo != null)
					fbo.blitFromTexture(
						Utils.mainColorTexture(),
						Utils.mainTargetWidth(),
						Utils.mainTargetHeight(),
						true
					);
			});
		}

		SodiumWorldRenderer sodiumWorld = ((LevelRendererExtension) this).sodium$getWorldRenderer();
		SodiumWorldRendererExt ext = (SodiumWorldRendererExt) sodiumWorld;
		RenderSectionManager manager = ext.getRenderSectionManager();
		if (manager == null)
			return;

		Iterator<ChunkRenderList> renderLists = manager.getRenderLists().iterator();
		while (renderLists.hasNext()) {
			ChunkRenderList renderList = renderLists.next();

			ByteIterator geometrySections = renderList.sectionsWithGeometryIterator(false);
			if (geometrySections != null)
				while (geometrySections.hasNext())
					processChunk(renderList.getRegion(), geometrySections.nextByteAsInt());
		}
	}

	private static void processChunk(RenderRegion region, int sectionIndex) {
		RenderSection section = region.getSection(sectionIndex);
		if (section == null) return;

		RenderRegionExt regionExt = (RenderRegionExt) region;

		regionExt.processChunk((RenderSectionExt) section, sectionIndex);
	}
}
