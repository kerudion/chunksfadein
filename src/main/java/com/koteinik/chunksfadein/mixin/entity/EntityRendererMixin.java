package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "getPositionOffset", at = @At(value = "RETURN"), cancellable = true)
    public void modifyGetPositionOffset(Entity entity, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (!Config.isModEnabled || !Config.isAnimationEnabled || entity.getWorld() == null)
            return;

        ChunkSectionPos chunkPos = ChunkSectionPos.from(entity.getPos());
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null)
            return;

        if(((SodiumWorldRendererExt) renderer).getRenderSectionManager() == null) {
            return;
        }

        float[] offset = ((SodiumWorldRendererExt) renderer).getAnimationOffset(
                chunkPos.getX(),
                chunkPos.getY(),
                chunkPos.getZ());
        if (offset == null)
            return;

        cir.setReturnValue(new Vec3d(offset[0], offset[1], offset[2]));
    }
}
