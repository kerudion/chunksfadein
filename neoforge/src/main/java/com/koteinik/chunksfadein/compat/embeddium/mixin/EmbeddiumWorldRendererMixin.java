package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionManagerExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(value = EmbeddiumWorldRenderer.class, remap = false)
public class EmbeddiumWorldRendererMixin implements SodiumWorldRendererExt {
	@Shadow
	private RenderSectionManager renderSectionManager;

	@Override
	public float[] getAnimationOffset(Vec3 pos) {
		SectionPos chunkPos = SectionPos.of(pos);
		float[] offset = ((RenderSectionManagerExt) renderSectionManager).getAnimationOffset(
			chunkPos.getX(),
			chunkPos.getY(),
			chunkPos.getZ()
		);

		if (Config.isCurvatureEnabled) {
			Minecraft client = Minecraft.getInstance();

			Entity camera = client.getCameraEntity();
			if (camera != null) {
				float len = (float) pos.subtract(camera.position()).length();

				if (offset == null)
					offset = new float[3];
				else
					offset = offset.clone();

				offset[1] -= (len * len) / Config.worldCurvature;
			}
		}

		return offset;
	}

	@Override
	public @Nullable RenderSectionManagerExt getRenderSectionManager() {
		return (RenderSectionManagerExt) renderSectionManager;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void modifyInit(Minecraft client, CallbackInfo ci) {
		SodiumWorldRendererExt.Holder.instance = this;
	}

	@Inject(
		method = "renderBlockEntity",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
			shift = At.Shift.AFTER,
			remap = true
		)
	)
	private static void modifySubmitBlockEntities(
		PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity, CallbackInfo ci
	) {
		if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled))
			return;

		SodiumWorldRendererExt ext = ((SodiumWorldRendererExt) EmbeddiumWorldRenderer.instance());
		if (ext.getRenderSectionManager() == null)
			return;

		float[] offset = ext.getAnimationOffset(entity.getBlockPos().getCenter());

		matrices.translate(offset[0], offset[1], offset[2]);
	}
}
