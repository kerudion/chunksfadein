package com.koteinik.chunksfadein.compat.dh.mixin.ogl;

import com.seibel.distanthorizons.common.render.openGl.GlDhTerrainRenderer_neoforge;
import com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram_neoforge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GlDhTerrainRenderer_neoforge.class, remap = false)
public interface GlDhTerrainRendererMixin_neoforge {
	@Accessor("terrainShaderProgram")
	void setTerrainShaderProgram(GlDhTerrainShaderProgram_neoforge value);
}
