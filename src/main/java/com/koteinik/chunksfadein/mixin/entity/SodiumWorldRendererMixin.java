package com.koteinik.chunksfadein.mixin.entity;

import java.util.SortedSet;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.RenderSectionManagerExt;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkSectionPos;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExt {
    @Shadow
    private RenderSectionManager renderSectionManager;

    @Override
    public float[] getAnimationOffset(int x, int y, int z) {
        return ((RenderSectionManagerExt) renderSectionManager).getAnimationOffset(x, y, z);
    }

    @Override
    public @Nullable RenderSectionManager getRenderSectionManager() {
        return renderSectionManager;
    }

    @Inject(method = "renderBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = Shift.AFTER, remap = true), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void modifyRenderTileEntities(MatrixStack matrices,
            BufferBuilderStorage bufferBuilders,
            Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions,
            float tickDelta,
            VertexConsumerProvider.Immediate immediate,
            double x,
            double y,
            double z,
            BlockEntityRenderDispatcher dispatcher,
            BlockEntity entity, CallbackInfo ci) {
        if (!Config.isModEnabled || !entity.hasWorld() || !Config.isAnimationEnabled)
            return;

        if(((SodiumWorldRendererExt) instance()).getRenderSectionManager() == null) {
            return;
        }

        ChunkSectionPos chunkPos = ChunkSectionPos.from(entity.getPos());

        float[] fadeData = ((SodiumWorldRendererExt) instance()).getAnimationOffset(
                chunkPos.getX(),
                chunkPos.getY(),
                chunkPos.getZ());

        matrices.translate(fadeData[0], fadeData[1], fadeData[2]);
    }

    @Shadow
    public static SodiumWorldRenderer instance() {
        return null;
    }
}
