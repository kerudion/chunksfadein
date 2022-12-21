package com.koteinik.chunksfadein.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ChunkAppearedLink;
import com.koteinik.chunksfadein.core.ChunkData;
import com.koteinik.chunksfadein.extenstions.EntityExt;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {
    private boolean needToTurnOff = Config.needToTurnOff();

    @Inject(method = "getPositionOffset", at = @At(value = "RETURN"), cancellable = true)
    public void modifeGetPositionOffset(Entity entity, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (!Config.isAnimationEnabled || needToTurnOff)
            return;

        EntityExt ext = (EntityExt) entity;

        ChunkPos chunkPos = entity.getChunkPos();
        int chunkY = MathUtils.floor((float) entity.getY() / 16f);

        ChunkSection chunk = entity.getWorld().getChunk(chunkPos.x, chunkPos.z).getSectionArray()[entity.getWorld()
                .sectionCoordToIndex(chunkY)];

        if (chunk.isEmpty()) {
            cir.setReturnValue(Vec3d.ZERO);
            ext.setLastRenderOffset(Vec3d.ZERO);
            return;
        }

        ChunkData fadeData = ChunkAppearedLink.getChunkData(chunkPos.x, chunkY, chunkPos.z);
        Vec3d offset = new Vec3d(fadeData.x, fadeData.y, fadeData.z);

        cir.setReturnValue(offset);
        ext.setLastRenderOffset(offset);
    }
}
