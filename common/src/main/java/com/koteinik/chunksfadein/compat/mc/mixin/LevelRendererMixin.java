package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.RenderPhase;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(
		method = "renderLevel",
		at = @At(value = "HEAD")
	)
	private void cfi_levelStart(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		RenderPhase.renderingLevel = true;
	}

	@Inject(
		method = "renderLevel",
		at = @At(value = "RETURN")
	)
	private void cfi_levelEnd(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		RenderPhase.renderingLevel = false;
	}

	@Inject(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/FogRenderer;setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V"
		)
	)
	private void modifyRenderLevel(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return;


		SkyFBO fbo = SkyFBO.getInstance();
		if (fbo != null)
			fbo.blitFromTexture(
				Utils.mainColorTexture(),
				Utils.mainTargetWidth(),
				Utils.mainTargetHeight(),
				true
			);
	}
}
