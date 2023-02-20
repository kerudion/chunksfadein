package com.koteinik.chunksfadein.mixin.iris;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionExt;
import com.koteinik.chunksfadein.hooks.IrisApiHook;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;

@Mixin(value = RegionChunkRenderer.class, remap = false)
public abstract class IrisRegionChunkRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RegionChunkRenderer;setModelMatrixUniforms", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    @SuppressWarnings("rawtypes")
    private void modifyChunkRender(ChunkRenderMatrices matrices, CommandList commandList,
            ChunkRenderList list, BlockRenderPass pass,
            ChunkCameraContext camera,
            CallbackInfo ci, ChunkShaderInterface shader, Iterator i, Map.Entry e, RenderRegion region,
            List<RenderSection> chunks) {
        if (!Config.isModEnabled || !IrisApiHook.isShaderPackInUse())
            return;

        final net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt rendererExt = (net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt) this;
        GlProgram<IrisChunkShaderInterface> override = rendererExt.iris$getOverride();
        if (override == null)
            return;

        final ChunkShaderInterfaceExt ext = (ChunkShaderInterfaceExt) (override.getInterface());
        final RenderRegionExt regionExt = (RenderRegionExt) region;
        regionExt.updateChunksFade(chunks, ext, commandList);
    }
}
