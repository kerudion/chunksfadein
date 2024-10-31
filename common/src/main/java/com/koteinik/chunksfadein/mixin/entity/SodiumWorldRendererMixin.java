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
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExt {
    @Shadow
    private RenderSectionManager renderSectionManager;

    @Override
    public float[] getAnimationOffset(Vec3 pos) {
        SectionPos chunkPos = SectionPos.of(pos);
        float[] offset = ((RenderSectionManagerExt) renderSectionManager).getAnimationOffset(chunkPos.getX(),
            chunkPos.getY(), chunkPos.getZ());

        if (Config.isCurvatureEnabled) {
            Minecraft client = Minecraft.getInstance();

            Entity camera = client.getCameraEntity();
            if (camera != null) {
                float len = (float) pos.subtract(camera.position()).length();

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

    @Inject(method = "renderBlockEntity", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", shift = Shift.AFTER, remap = true),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private static void modifyRenderTileEntities(PoseStack matrices,
        RenderBuffers bufferBuilders,
        Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
        float tickDelta,
        MultiBufferSource.BufferSource immediate,
        double x, double y, double z,
        BlockEntityRenderDispatcher dispatcher,
        BlockEntity entity,
        LocalPlayer player,
        LocalBooleanRef ref,
        CallbackInfo ci) {
        if (!Config.isModEnabled || !entity.hasLevel() || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled))
            return;

        if (((SodiumWorldRendererExt) instance()).getRenderSectionManager() == null)
            return;

        float[] offset = ((SodiumWorldRendererExt) instance()).getAnimationOffset(entity.getBlockPos().getCenter());

        matrices.translate(offset[0], offset[1], offset[2]);
    }

    @Shadow
    public static SodiumWorldRenderer instance() {
        return null;
    }
}
