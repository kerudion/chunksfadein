package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "getRenderOffset", at = @At(value = "RETURN"), cancellable = true)
    public void modifyGetPositionOffsetNew(EntityRenderState state, CallbackInfoReturnable<Vec3> cir) {
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
    
        cir.setReturnValue(new Vec3(offset[0], offset[1], offset[2]));
    }
}
