package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.ChunkUtils;
import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ChunkAppearedLink;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "getPositionOffset", at = @At(value = "RETURN"), cancellable = true)
    public void modifyGetPositionOffset(Entity entity, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
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
        Vec3d offset = new Vec3d(fadeData[0], fadeData[1], fadeData[2]);

        cir.setReturnValue(offset);
    }
}
