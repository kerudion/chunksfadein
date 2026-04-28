package com.koteinik.chunksfadein.compat.dh.mixin;

import com.seibel.distanthorizons.common.render.openGl.GlDhTerrainRenderer;
import com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GlDhTerrainRenderer.class, remap = false)
public interface GlDhTerrainRendererMixin {
	@Accessor("terrainShaderProgram")
	void setTerrainShaderProgram(GlDhTerrainShaderProgram value);
}
