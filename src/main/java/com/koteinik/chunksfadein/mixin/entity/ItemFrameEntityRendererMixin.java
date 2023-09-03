package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

@Mixin(value = ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V", ordinal = 1, shift = Shift.BEFORE))
    private void modifyRender(ItemFrameEntity entity, float f, float g, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!Config.isModEnabled || !Config.isAnimationEnabled || entity.getWorld() == null)
            return;

        ChunkSectionPos chunkPos = ChunkSectionPos.from(new BlockPos(entity.getPos()));
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null)
            return;

        float[] offset = ((SodiumWorldRendererExt) renderer).getAnimationOffset(
                chunkPos.getX(),
                chunkPos.getY(),
                chunkPos.getZ());
        if (offset == null)
            return;

        matrixStack.translate(offset[0], offset[1], offset[2]);
    }
}
