package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionManagerExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
				Vec3 cam = camera.position();

				double x = pos.x - cam.x;
				double z = pos.z - cam.z;

				if (offset == null)
					offset = new float[3];
				else
					offset = offset.clone();

				offset[1] -= (float) ((x * x + z * z) / Config.worldCurvature);
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

	@WrapOperation(
		method = {
			"renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;)V",
			"renderGlobalBlockEntities"
		},
		at = @At(
			value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/EmbeddiumWorldRenderer;renderBlockEntity(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"
		)
	)
	private void modifySubmitBlockEntities(
		PoseStack matrices,
		RenderBuffers bufferBuilders,
		Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
		float tickDelta,
		MultiBufferSource.BufferSource immediate,
		double x, double y, double z,
		BlockEntityRenderDispatcher dispatcher,
		BlockEntity entity,
		Operation<Void> original
	) {
		float[] offset = null;

		if (Config.isModEnabled && (Config.isAnimationEnabled || Config.isCurvatureEnabled)
			&& getRenderSectionManager() != null)
			offset = getAnimationOffset(entity.getBlockPos().getCenter());

		if (offset != null) {
			matrices.pushPose();
			matrices.translate(offset[0], offset[1], offset[2]);
		}

		original.call(
			matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate,
			x, y, z, dispatcher, entity
		);

		if (offset != null)
			matrices.popPose();
	}
}
