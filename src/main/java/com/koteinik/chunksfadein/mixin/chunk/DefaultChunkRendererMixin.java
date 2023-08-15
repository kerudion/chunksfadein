package com.koteinik.chunksfadein.mixin.chunk;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extensions.RenderRegionExt;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.MultiDrawBatch;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import me.jellysquid.mods.sodium.client.util.iterator.ByteIterator;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class DefaultChunkRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;executeDrawBatch", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyChunkRender(ChunkRenderMatrices matrices,
            CommandList commandList,
            ChunkRenderListIterable renderLists,
            TerrainRenderPass renderPass,
            CameraTransform camera,
            CallbackInfo ci, boolean useBlockFaceCulling, ChunkShaderInterface shader,
            Iterator<ChunkRenderList> iterator,
            ChunkRenderList renderList, RenderRegion region) {
        if (!Config.isModEnabled || shader == null)
            return;

        // Made to not interrupt Axiom mixin
        uploadToBuffer(commandList, shader, region);
    }

    @Inject(method = "fillCommandBuffer", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;addDrawCommands", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void modifyFillCommandBuffer(MultiDrawBatch batch,
            RenderRegion region,
            SectionRenderDataStorage renderDataStorage,
            ChunkRenderList renderList,
            CameraTransform camera,
            TerrainRenderPass pass,
            boolean useBlockFaceCulling,
            CallbackInfo ci, ByteIterator iterator, int originX, int originY, int originZ, int sectionIndex, int x,
            int y, int z, long pMeshData, int slices) {
        if (!Config.isModEnabled)
            return;

        // Made to not interrupt Axiom mixin
        processChunk(region, sectionIndex, x, y, z);
    }

    private void uploadToBuffer(CommandList commandList, ChunkShaderInterface shader, RenderRegion region) {
        final ChunkShaderInterfaceExt ext = (ChunkShaderInterfaceExt) shader;
        final RenderRegionExt regionExt = (RenderRegionExt) region;

        regionExt.uploadToBuffer(ext, commandList);
    }

    private static void processChunk(RenderRegion region, int sectionIndex, int x, int y, int z) {
        final RenderSection section = region.getSection(sectionIndex);

        final RenderRegionExt regionExt = (RenderRegionExt) region;

        regionExt.processChunk((RenderSectionExt) section, sectionIndex, x, y, z);
    }
}
