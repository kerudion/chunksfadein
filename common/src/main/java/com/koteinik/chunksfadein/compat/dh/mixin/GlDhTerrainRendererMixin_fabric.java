package com.koteinik.chunksfadein.compat.dh.mixin;

import com.seibel.distanthorizons.common.render.openGl.GlDhTerrainRenderer_fabric;
import com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram_fabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GlDhTerrainRenderer_fabric.class, remap = false)
public interface GlDhTerrainRendererMixin_fabric {
	@Accessor("terrainShaderProgram")
	void setTerrainShaderProgram(GlDhTerrainShaderProgram_fabric value);
}
