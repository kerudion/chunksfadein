package com.koteinik.chunksfadein.mixin;

import java.util.Iterator;
import java.util.SortedSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ChunkAppearedLink;
import com.koteinik.chunksfadein.core.ChunkData;
import com.koteinik.chunksfadein.extenstions.BlockEntityExt;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin {
    @Inject(method = "renderTileEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    @SuppressWarnings("rawtypes")
    private void modifyRender(MatrixStack matrices, BufferBuilderStorage bufferBuilders,
            Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions,
            Camera camera, float tickDelta, CallbackInfo ci, Immediate i1, Vec3d v, double d1, double d2, double d3,
            BlockEntityRenderDispatcher d, Iterator i2, BlockEntity entity) {
        if (!entity.hasWorld() || !Config.isAnimationEnabled || needToTurnOff)
            return;

        BlockEntityExt ext = (BlockEntityExt) entity;

        ChunkSectionPos chunkPos = ChunkSectionPos.from(entity.getPos());

        ChunkSection chunk = entity.getWorld().getChunk(chunkPos.getX(), chunkPos.getZ())
                .getSectionArray()[entity.getWorld().sectionCoordToIndex(chunkPos.getY())];

        if (chunk.isEmpty()) {
            ext.setLastRenderOffset(Vec3d.ZERO);
            return;
        }

        ChunkData fadeData = ChunkAppearedLink.getChunkData(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
        Vec3d offset = new Vec3d(fadeData.x, fadeData.y, fadeData.z);

        matrices.translate(fadeData.x, fadeData.y, fadeData.z);
        ext.setLastRenderOffset(offset);
    }

    @Inject(method = "isEntityVisible", at = @At(value = "RETURN"), cancellable = true)
    private void modifyIsEntityVisible(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(needToTurnOff)
            return;

        if (cir.getReturnValueZ()) {
            ChunkPos chunkPos = entity.getChunkPos();
            int chunkY = MathUtils.floor((float) entity.getY() / 16f);

            ChunkData fadeData = ChunkAppearedLink.getChunkData(chunkPos.x, chunkY,
                    chunkPos.z);

            boolean isVisible = !(fadeData.y == -Config.animationInitialOffset && fadeData.fadeCoeff == 0f);

            if (!isVisible) {
                ChunkSection chunk = entity.getWorld().getChunk(chunkPos.x, chunkPos.z)
                        .getSectionArray()[entity.getWorld().sectionCoordToIndex(chunkY)];

                if (chunk.isEmpty())
                    isVisible = true;
            }

            cir.setReturnValue(isVisible);
        }
    }

}
