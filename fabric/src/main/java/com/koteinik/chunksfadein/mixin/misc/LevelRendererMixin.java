package com.koteinik.chunksfadein.mixin.misc;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Final
	private LevelTargetBundle targets;

	@Inject(method = "renderLevel", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/LevelRenderer;addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/FogParameters;)V",
		shift = At.Shift.AFTER))
	private void modifyRenderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, Matrix4f matrix4f, Matrix4f matrix4f2,
	                               CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder, @Local FramePass skyPass) {
		FramePass framePass = frameGraphBuilder.addPass("cfi_blit_sky");
		targets.main = framePass.readsAndWrites(targets.main);
		framePass.requires(skyPass);

		framePass.executes(() -> {
			if (!Config.isModEnabled || !Config.isFadeEnabled)
				return;

			SkyFBO fbo = SkyFBO.getInstance();
			if (fbo != null)
				fbo.blitFromTexture(Utils.mainColorTexture(), Utils.mainTargetWidth(), Utils.mainTargetHeight(), true);
		});
	}
}
