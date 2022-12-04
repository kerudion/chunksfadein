package com.koteinik.chunksfadein.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionArenasExt;

import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;

@Mixin(value = RegionChunkRenderer.class, remap = false)
public class RegionChunkRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RegionChunkRenderer;setModelMatrixUniforms", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyChunkRender(ChunkRenderMatrices matrices, CommandList commandList,
            ChunkRenderList list, BlockRenderPass pass,
            ChunkCameraContext camera,
            CallbackInfo ci, ChunkShaderInterface shader, Iterator i, Map.Entry e, RenderRegion region, List<RenderSection> chunks) {
        final ChunkShaderInterfaceExt ext = (ChunkShaderInterfaceExt) shader;
        final RenderRegionArenasExt arenas = (RenderRegionArenasExt) region.getArenas();
        arenas.updateChunksFade(chunks, ext);
    }
}
