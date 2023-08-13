package com.koteinik.chunksfadein.mixin.entity;

import java.util.Iterator;
import java.util.SortedSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExt {
    @Shadow
    private RenderSectionManager renderSectionManager;

    @Override
    public float[] getAnimationOffset(int x, int y, int z) {
        return ((RenderSectionManagerExt) renderSectionManager).getAnimationOffset(x, y, z);
    }

    @Override
    public float getFadeCoeff(int x, int y, int z) {
        return ((RenderSectionManagerExt) renderSectionManager).getFadeCoeff(x, y, z);
    }

    @Inject(method = "renderTileEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = Shift.AFTER, remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyRenderTileEntities(MatrixStack matrices, BufferBuilderStorage bufferBuilders,
            Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions, Camera camera, float tickDelta,
            CallbackInfo ci, Immediate immediate, Vec3d cameraPos, double x, double y, double z,
            BlockEntityRenderDispatcher blockEntityRenderer, Iterator<?> var15, BlockEntity entity, BlockPos pos) {
        if (!Config.isModEnabled || !entity.hasWorld() || !Config.isAnimationEnabled)
            return;

        ChunkSectionPos chunkPos = ChunkSectionPos.from(entity.getPos());

        float[] fadeData = getAnimationOffset(
                chunkPos.getX(),
                chunkPos.getY(),
                chunkPos.getZ());
        if (fadeData == null)
            return;

        matrices.translate(fadeData[0], fadeData[1], fadeData[2]);
    }

    @Inject(method = "isEntityVisible", at = @At(value = "RETURN"), cancellable = true)
    private void modifyIsEntityVisible(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.isModEnabled || entity.getWorld() == null || !cir.getReturnValueZ())
            return;

        ChunkSectionPos chunkPos = ChunkSectionPos.from(entity.getPos());

        float fadeCoeff = getFadeCoeff(
                chunkPos.getX(),
                chunkPos.getY(),
                chunkPos.getZ());

        if (fadeCoeff == 0f)
            cir.setReturnValue(false);
    }

    @Shadow
    public static SodiumWorldRenderer instance() {
        return null;
    }
}
