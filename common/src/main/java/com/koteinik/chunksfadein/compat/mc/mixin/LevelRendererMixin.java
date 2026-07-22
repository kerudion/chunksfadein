package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.RenderPhase;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;
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
		at = @At(value = "HEAD")
	)
	private void cfi_levelStart(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {
		RenderPhase.renderingLevel = true;
	}

	@Inject(
		method = "renderLevel",
		at = @At(value = "RETURN")
	)
	private void cfi_levelEnd(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {
		RenderPhase.renderingLevel = false;
	}

	@Inject(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V"
		)
	)
	private void modifyRenderLevel(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
		if (!Config.isModEnabled)
			return;

		if (Config.isFadeEnabled) {
			FramePass framePass = frameGraphBuilder.addPass("cfi_blit_sky");
			targets.main = framePass.readsAndWrites(targets.main);

			framePass.executes(() -> {
				SkyFBO fbo = SkyFBO.getInstance();
				if (fbo != null)
					fbo.blitFromTexture(Utils.mainColorTexture());
			});
		}

		SodiumWorldRenderer sodiumWorld = ((LevelRendererExtension) this).sodium$getWorldRenderer();
		SodiumWorldRendererExt ext = (SodiumWorldRendererExt) sodiumWorld;
		RenderSectionManager manager = ext.getRenderSectionManager();
		if (manager == null)
			return;

		if (CompatibilityHook.isDHRenderingEnabled())
			LodMaskTexture.prepare();

		Iterator<ChunkRenderList> renderLists = manager.getRenderLists().iterator();
		while (renderLists.hasNext()) {
			ChunkRenderList renderList = renderLists.next();

			ByteIterator geometrySections = renderList.sectionsWithGeometryIterator(false);
			if (geometrySections != null)
				while (geometrySections.hasNext())
					processChunk((RenderRegionExt) renderList.getRegion(), geometrySections.nextByteAsInt());
		}

		if (CompatibilityHook.isDHRenderingEnabled())
			LodMaskTexture.upload();
	}

	@Inject(
		method = "submitBlockEntities",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
			shift = At.Shift.AFTER
		)
	)
	private void modifySubmitBlockEntities(
		PoseStack matrices,
		LevelRenderState levelRenderState,
		SubmitNodeStorage submitNodeStorage,
		CallbackInfo ci,
		@Local BlockEntityRenderState state
	) {
		if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled))
			return;

		SodiumWorldRendererExt ext = ((SodiumWorldRendererExt) SodiumWorldRenderer.instance());
		if (ext.getRenderSectionManager() == null)
			return;

		float[] offset = ext.getAnimationOffset(state.blockPos.getCenter());
		if (offset == null)
			return;

		matrices.translate(offset[0], offset[1], offset[2]);
	}

	private static void processChunk(RenderRegionExt region, int sectionIndex) {
		RenderSection section = region.getSection(sectionIndex);
		if (section == null) return;

		region.processChunk((RenderSectionExt) section, sectionIndex);
	}
}
