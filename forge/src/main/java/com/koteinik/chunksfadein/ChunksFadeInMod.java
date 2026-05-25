package com.koteinik.chunksfadein;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(value = "chunksfadein")
public class ChunksFadeInMod {
	public ChunksFadeInMod() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ChunksFadeIn::init);
	}
}
