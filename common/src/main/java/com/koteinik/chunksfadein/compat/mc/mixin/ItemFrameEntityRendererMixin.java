package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemFrameRenderer.class)
public class ItemFrameEntityRendererMixin {
	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
	)
	private void modifyRender(ItemFrameRenderState state, PoseStack stack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
		if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled) || state.isDiscrete)
			return;

		SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
		if (renderer == null)
			return;

		if (((SodiumWorldRendererExt) renderer).getRenderSectionManager() == null)
			return;

		float[] offset = ((SodiumWorldRendererExt) renderer).getAnimationOffset(new Vec3(state.x, state.y, state.z));
		if (offset == null)
			return;

		stack.translate(offset[0], offset[1], offset[2]);
	}
}
