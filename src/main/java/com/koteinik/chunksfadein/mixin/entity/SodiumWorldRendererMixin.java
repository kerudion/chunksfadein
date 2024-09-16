package com.koteinik.chunksfadein.mixin.entity;

import java.util.SortedSet;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.RenderSectionManagerExt;
import com.koteinik.chunksfadein.extensions.SodiumWorldRendererExt;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExt {
    @Shadow
    private RenderSectionManager renderSectionManager;

    @Override
    public float[] getAnimationOffset(Vec3d pos) {
        ChunkSectionPos chunkPos = ChunkSectionPos.from(pos);
        float[] offset = ((RenderSectionManagerExt) renderSectionManager).getAnimationOffset(chunkPos.getX(),
            chunkPos.getY(), chunkPos.getZ());

        if (Config.isCurvatureEnabled) {
            MinecraftClient client = MinecraftClient.getInstance();

            Entity camera = client.getCameraEntity();
            if (camera != null) {
                float len = (float) pos.subtract(camera.getPos()).length();

                if (offset == null)
                    offset = new float[3];
                else
                    offset = offset.clone();

                offset[1] -= (len * len) / Config.worldCurvature;
            }
        }

        return offset;
    }

    @Override
    public @Nullable RenderSectionManager getRenderSectionManager() {
        return renderSectionManager;
    }

    @Inject(method = "renderBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = Shift.AFTER, remap = true),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private static void modifyRenderTileEntities(MatrixStack matrices,
        BufferBuilderStorage bufferBuilders,
        Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions,
        float tickDelta,
        Immediate immediate,
        double x, double y, double z,
        BlockEntityRenderDispatcher dispatcher,
        BlockEntity entity,
        ClientPlayerEntity player,
        LocalBooleanRef ref,
        CallbackInfo ci) {
        if (!Config.isModEnabled || !entity.hasWorld() || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled))
            return;

        if (((SodiumWorldRendererExt) instance()).getRenderSectionManager() == null)
            return;

        float[] offset = ((SodiumWorldRendererExt) instance()).getAnimationOffset(entity.getPos().toCenterPos());

        matrices.translate(offset[0], offset[1], offset[2]);
    }

    @Shadow
    public static SodiumWorldRenderer instance() {
        return null;
    }
}
