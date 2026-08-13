package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import net.caffeinemc.mods.sodium.client.gl.arena.ArenaAggregator;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExt {
	@Shadow
	@Final
	private RenderSection[] sections;
	private ChunkFadeInController fadeController;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void modifyConstructor(int x, int y, int z, ArenaAggregator arenaAggregator, CallbackInfo ci) {
		fadeController = new ChunkFadeInController(GLRenderDevice.INSTANCE.createCommandList());
	}

	@Inject(method = "delete", at = @At(value = "TAIL"))
	private void modifyDeleteResources(CommandList commandList, CallbackInfo ci) {
		fadeController.delete(commandList);
	}

	@Override
	public void processChunk(RenderSectionExt section, int sectionIndex) {
		fadeController.processChunk(section, sectionIndex);
	}

	@Override
	public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandList commandList) {
		fadeController.uploadToBuffer(shader, commandList);
	}

	@Override
	public RenderSection getSection(int sectionIndex) {
		return sections[sectionIndex];
	}
}
