package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "getRenderOffset", at = @At(value = "RETURN"), cancellable = true)
    public void modifyGetPositionOffsetNew(Entity entity, float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled) || entity.level() == null || entity.level().getEntity(entity.getId()) == null)
            return;
    
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null)
            return;
    
        if (((SodiumWorldRendererExt) renderer).getRenderSectionManager() == null)
            return;
    
        float[] offset = ((SodiumWorldRendererExt) renderer).getAnimationOffset(entity.position());
        if (offset == null)
            return;
    
        cir.setReturnValue(new Vec3(offset[0], offset[1], offset[2]));
    }
}
