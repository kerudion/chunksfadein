package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionManagerExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExt {
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
	public @Nullable RenderSectionManager getRenderSectionManager() {
		return renderSectionManager;
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
		PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity, LocalPlayer player, LocalBooleanRef isGlowing, CallbackInfo ci
	) {
		if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled))
			return;

		SodiumWorldRendererExt ext = ((SodiumWorldRendererExt) SodiumWorldRenderer.instance());
		if (ext.getRenderSectionManager() == null)
			return;

		float[] offset = ext.getAnimationOffset(entity.getBlockPos().getCenter());

		matrices.translate(offset[0], offset[1], offset[2]);
	}
}
