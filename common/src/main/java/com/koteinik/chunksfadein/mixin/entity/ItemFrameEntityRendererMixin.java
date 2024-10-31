package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;
import com.mojang.blaze3d.vertex.PoseStack;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;

@Mixin(value = ItemFrameRenderer.class)
public class ItemFrameEntityRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 1, shift = Shift.BEFORE))
    private void modifyRender(ItemFrame entity, float f, float g, PoseStack stack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled) || entity.level() == null)
            return;

        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null)
            return;

        if (((SodiumWorldRendererExt) renderer).getRenderSectionManager() == null)
            return;

        float[] offset = ((SodiumWorldRendererExt) renderer).getAnimationOffset(entity.position());
        if (offset == null)
            return;

        stack.translate(offset[0], offset[1], offset[2]);
    }
}
