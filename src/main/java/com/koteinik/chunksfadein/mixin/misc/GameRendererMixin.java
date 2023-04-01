package com.koteinik.chunksfadein.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.core.FrameData;

import net.minecraft.client.render.GameRenderer;

@Mixin(value = GameRenderer.class)
public class GameRendererMixin {
    private long lastFrameTime = 0L;
    long currentFrameTime;

    @Inject(method = "render", at = @At("HEAD"))
    private void modifyRenderWorld(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        currentFrameTime = System.nanoTime();
        FrameData.frameDelta = lastFrameTime == 0L ? 0 : (float) (currentFrameTime - lastFrameTime) / 1000000;
        lastFrameTime = currentFrameTime;
    }
}