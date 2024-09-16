package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkSectionPos;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin implements RenderSectionExt {
    private static final float[] FULLY_ANIMATED_OFFSET = new float[] { 0f, 0f, 0f };

    @Shadow
    @Final
    private RenderRegion region;

    @Shadow
    @Final
    private int chunkX;

    @Shadow
    @Final
    private int chunkY;

    @Shadow
    @Final
    private int chunkZ;

    private long lastFrameTime = 0L;
    private boolean hasRenderedBefore;
    private float fadeCoeff = 0f;
    private float animationProgress = 0f;

    private final float[] offset = new float[] { 0f, 0f, 0f };

    @Override
    public boolean hasRenderedBefore() {
        return hasRenderedBefore;
    }

    @Override
    public void setRenderedBefore() {
        hasRenderedBefore = true;
    }

    @Override
    public float incrementFadeCoeff(long delta) {
        if (fadeCoeff == 1f)
            return 1f;

        fadeCoeff += delta * Config.fadeChangePerNano;

        if (fadeCoeff > 1f)
            fadeCoeff = 1f;

        if (!hasRenderedBefore() && !Config.fadeNearPlayer && isNearPlayer())
            fadeCoeff = 1f;

        return fadeCoeff;
    }

    @Override
    public float[] incrementAnimationOffset(long delta) {
        if (animationProgress == 1f)
            return FULLY_ANIMATED_OFFSET;

        animationProgress += delta * Config.animationChangePerNano;
        if (animationProgress > 1f)
            animationProgress = 1f;

        if (!hasRenderedBefore() && !Config.animateNearPlayer && isNearPlayer())
            animationProgress = 1f;

        float animY;
        float curved = Config.animationInitialOffset
            - Config.animationCurve.calculate(animationProgress) * Config.animationInitialOffset;
        curved = -curved;

        if (curved > 0f)
            animY = 0f;
        else
            animY = curved;

        offset[1] = animY;

        return offset;
    }

    @Override
    public long calculateAndGetDelta() {
        long currentFrameTime = System.currentTimeMillis();
        long delta = lastFrameTime == 0L ? 0L : currentFrameTime - lastFrameTime;

        lastFrameTime = currentFrameTime;

        return delta * 1000000;
    }

    @Override
    public float[] getAnimationOffset() {
        return offset;
    }

    @Override
    public float getFadeCoeff() {
        return fadeCoeff;
    }

    private boolean isNearPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity camera = client.cameraEntity;

        if (camera != null) {
            ChunkSectionPos chunkPos = ChunkSectionPos.from(camera.getPos());

            final int camChunkX = chunkPos.getX();
            final int camChunkZ = chunkPos.getZ();
            final int x = chunkX;
            final int z = chunkZ;

            return MathUtils.chunkInRange(x, z, camChunkX, camChunkZ, 1);
        }

        return false;
    }
}
