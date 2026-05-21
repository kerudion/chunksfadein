package com.koteinik.chunksfadein.compat.dh.mixin;

import com.seibel.distanthorizons.common.render.openGl.GlDhTerrainRenderer_forge;
import com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram_forge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GlDhTerrainRenderer_forge.class, remap = false)
public interface GlDhTerrainRendererMixin_forge {
	@Accessor("terrainShaderProgram")
	void setTerrainShaderProgram(GlDhTerrainShaderProgram_forge value);
}
