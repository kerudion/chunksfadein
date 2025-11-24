
package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemFrameRenderer.class)
public class ItemFrameEntityRendererMixin {
	@Inject(
		method = "render(Lnet/minecraft/world/entity/decoration/ItemFrame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
			ordinal = 1
		)
	)
	private void modifyRender(ItemFrame entity, float f, float g, PoseStack stack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
		if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled)
			|| entity.level() == null)
			return;

		SodiumWorldRendererExt renderer = SodiumWorldRendererExt.Holder.instance;
		if (renderer == null)
			return;

		if (renderer.getRenderSectionManager() == null)
			return;

		float[] offset = renderer.getAnimationOffset(entity.position());
		if (offset == null)
			return;

		stack.translate(offset[0], offset[1], offset[2]);
	}
}
