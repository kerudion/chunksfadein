package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
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
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
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
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V"
		)
	)
	private void modifyRenderLevel(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
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

		SodiumWorldRendererExt worldRenderer = (SodiumWorldRendererExt) ((LevelRendererExtension) this).sodium$getWorldRenderer();
		RenderSectionManager sectionManager = worldRenderer.getRenderSectionManager();
		if (sectionManager == null)
			return;

		ChunkFadeInController controller = worldRenderer.getChunkFadeInController();
		if (controller == null)
			return;

		controller.updateUniforms();

		if (CompatibilityHook.isDHRenderingEnabled())
			LodMaskTexture.prepare();

		Iterator<ChunkRenderList> renderLists = sectionManager.getRenderLists().iterator();
		while (renderLists.hasNext()) {
			ChunkRenderList renderList = renderLists.next();

			RenderRegion region = renderList.getRegion();
			int regionIndex = region.getId();
			if (regionIndex == -1) continue;

			RenderRegionExt regionExt = (RenderRegionExt) region;

			ByteIterator geometrySections = renderList.sectionsWithGeometryIterator(false);
			if (geometrySections == null) continue;

			while (geometrySections.hasNext()) {
				int sectionIndex = geometrySections.nextByteAsInt();

				RenderSectionExt section = regionExt.getSection(sectionIndex);
				if (section == null) continue;

				controller.processChunk(section, regionIndex, sectionIndex);
			}
		}

		controller.uploadToBuffer();

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
		SubmitNodeCollector submitNodeCollector,
		CallbackInfo ci,
		@Local BlockEntityRenderState state
	) {
		if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled))
			return;

		SodiumWorldRendererExt ext = ((SodiumWorldRendererExt) SodiumWorldRenderer.instance());
		if (ext.getRenderSectionManager() == null)
			return;

		BlockPos pos = state.blockPos;
		float[] offset = ext.getAnimationOffset(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
		if (offset == null)
			return;

		matrices.translate(offset[0], offset[1], offset[2]);
	}
}
