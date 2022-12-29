package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.ChunkUtils;
import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ChunkAppearedLink;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;

@Mixin(value = ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", ordinal = 1, shift = Shift.BEFORE))
    private void modifyRender(ItemFrameEntity entity, float f, float g, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        if (!Config.isAnimationEnabled)
            return;

        ChunkPos chunkPos = entity.getChunkPos();
        int chunkY = MathUtils.floor((float) entity.getY() / 16f);

        ChunkSection chunk = ChunkUtils.getChunkOn(entity.getWorld(), chunkPos, chunkY);

        if (chunk == null || chunk.isEmpty())
            return;

        float[] fadeData = ChunkAppearedLink.getChunkData(chunkPos.x, chunkY, chunkPos.z);
        matrixStack.translate(fadeData[0], fadeData[1], fadeData[2]);
    }
}
