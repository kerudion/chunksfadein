package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;

@Mixin(value = ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", ordinal = 1, shift = Shift.BEFORE))
    private void modifyRender(ItemFrameEntity entity, float f, float g, MatrixStack matrixStack,
        VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled) || entity.getWorld() == null)
            return;

        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null)
            return;

        if (((SodiumWorldRendererExt) renderer).getRenderSectionManager() == null)
            return;

        float[] offset = ((SodiumWorldRendererExt) renderer).getAnimationOffset(entity.getPos());
        if (offset == null)
            return;

        matrixStack.translate(offset[0], offset[1], offset[2]);
    }
}
