package com.koteinik.chunksfadein.compat.sodium.ext;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface SodiumWorldRendererExt {
	float[] getAnimationOffset(Vec3 pos);

	@Nullable
	RenderSectionManager getRenderSectionManager();

	@Nullable
	ChunkFadeInController getChunkFadeInController();
}
