package com.koteinik.chunksfadein.mixin.chunk;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.AnimationType;
import com.koteinik.chunksfadein.core.DataBuffer;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin implements RenderSectionExt {
    private static final Vector3f UP = new Vector3f(0, 1, 0);

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

    private final float[] offset = new float[3];

    @Override
    public boolean hasRenderedBefore() {
        return hasRenderedBefore;
    }

    @Override
    public void setRenderedBefore() {
        hasRenderedBefore = true;
    }

    @Override
    public boolean incrementFadeCoeff(long delta, int sectionIndex, DataBuffer buffer) {
        if (fadeCoeff == 1f)
            return false;

        fadeCoeff += delta * Config.fadeChangePerMs;

        if (fadeCoeff > 1f)
            fadeCoeff = 1f;

        if (!hasRenderedBefore() && !Config.fadeNearPlayer && isNearPlayer())
            fadeCoeff = 1f;

        buffer.put(sectionIndex, 3, fadeCoeff);

        return true;
    }

    @Override
    public boolean incrementAnimationOffset(long delta, int sectionIndex, DataBuffer buffer) {
        if (animationProgress == 1f)
            return false;

        animationProgress += delta * Config.animationChangePerMs;
        if (animationProgress > 1f)
            animationProgress = 1f;

        if (!hasRenderedBefore() && !Config.animateNearPlayer && isNearPlayer())
            animationProgress = 1f;

        float progress = Config.animationCurve.calculate(animationProgress);
        if (Config.animationType == AnimationType.JAGGED) {
            offset[1] = MathUtils.lerp(-Config.animationFactor * 16, 0, progress);
        } else if (Config.animationType == AnimationType.DISPLACEMENT || Config.animationType == AnimationType.SCALE) {
            offset[1] = MathUtils.lerp(Config.animationFactor, 0, progress);
        } else {
            if (Config.animationAngle == 0) {
                offset[1] = MathUtils.lerp(Config.animationOffset, 0, progress);
            } else {
                Vec3 thisPos = new Vec3(chunkX * 16 + 8, 0, chunkZ * 16 + 8);

                Vec3 camPos = getCameraPosition();
                camPos = new Vec3(camPos.x, 0, camPos.z);

                Vector3f direction = camPos.toVector3f().sub(thisPos.toVector3f()).normalize();
                Vector3f axis = new Vector3f(direction).cross(UP);

                direction.rotateAxis((float) Math.toRadians(90 - Config.animationAngle), axis.x, axis.y, axis.z)
                    .mul(Config.animationOffset)
                    .lerp(new Vector3f(), progress);

                if (Config.animationOffset > 0)
                    direction.rotateY((float) Math.PI);

                offset[0] = direction.x;
                offset[1] = direction.y;
                offset[2] = direction.z;
            }
        }

        for (int i = 0; i < 3; i++)
            buffer.put(sectionIndex, i, offset[i]);

        return true;
    }

    @Override
    public long calculateAndGetDelta() {
        long currentFrameTime = System.currentTimeMillis();
        long delta = lastFrameTime == 0L ? 0L : currentFrameTime - lastFrameTime;

        lastFrameTime = currentFrameTime;

        return delta;
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
        SectionPos chunkPos = SectionPos.of(getCameraPosition());

        final int camChunkX = chunkPos.getX();
        final int camChunkZ = chunkPos.getZ();

        return MathUtils.chunkInRange(chunkX, chunkZ, camChunkX, camChunkZ, 1);
    }

    private static Vec3 getCameraPosition() {
        Minecraft client = Minecraft.getInstance();
        Entity camera = client.cameraEntity;

        if (camera == null)
            return new Vec3(0, 0, 0);

        return camera.position();
    }
}
