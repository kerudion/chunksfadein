package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extensions.RenderRegionExt;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.MultiDrawBatch;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.LocalSectionIndex;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList.ReversibleSectionIterator;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.util.ReversibleArrayIterator;

@Mixin(value = RegionChunkRenderer.class, remap = false)
public abstract class IrisRegionChunkRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RegionChunkRenderer;executeDrawBatch", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyChunkRender(ChunkRenderMatrices matrices, CommandList commandList,
            RenderRegionManager regions, SortedRenderLists renderLists, TerrainRenderPass renderPass,
            ChunkCameraContext camera,
            CallbackInfo ci, ChunkShaderInterface shader, ReversibleArrayIterator<ChunkRenderList> regionIterator,
            ChunkRenderList renderList, RenderRegion region) {
        if (!Config.isModEnabled || !CompatibilityHook.isIrisShaderPackInUse())
            return;

        final ChunkShaderInterfaceExt ext = (ChunkShaderInterfaceExt) shader;
        final RenderRegionExt regionExt = (RenderRegionExt) region;

        regionExt.uploadToBuffer(ext, commandList);
    }

    @Inject(method = "fillCommandBuffer", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RegionChunkRenderer;addDrawCommands", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void modifyChunkRender(MultiDrawBatch batch, SectionRenderDataStorage storage, ChunkRenderList list,
            TerrainRenderPass pass,
            CallbackInfo ci, ReversibleSectionIterator sectionIterator, int next, int sectionIndex,
            int sectionCulledFaces) {
        if (!Config.isModEnabled || !CompatibilityHook.isIrisShaderPackInUse())
            return;

        final RenderRegion region = list.getRegion();
        final RenderSection section = region.getSection(sectionIndex);

        final int originX = region.getChunkX();
        final int originY = region.getChunkY();
        final int originZ = region.getChunkZ();

        final RenderRegionExt regionExt = (RenderRegionExt) region;
        final int x = originX + LocalSectionIndex.unpackX(sectionIndex);
        final int y = originY + LocalSectionIndex.unpackY(sectionIndex);
        final int z = originZ + LocalSectionIndex.unpackZ(sectionIndex);

        regionExt.processChunk((RenderSectionExt) section, sectionIndex, x, y, z);
    }
}
