package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionManagerExt;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExt {
	@Shadow
	private RenderSectionManager renderSectionManager;

	@Override
	public float[] getAnimationOffset(Vec3 pos) {
		SectionPos chunkPos = SectionPos.of(pos);
		float[] offset = ((RenderSectionManagerExt) renderSectionManager).getAnimationOffset(
			chunkPos.getX(),
			chunkPos.getY(),
			chunkPos.getZ()
		);

		if (Config.isCurvatureEnabled) {
			Minecraft client = Minecraft.getInstance();

			Entity camera = client.getCameraEntity();
			if (camera != null) {
				Vec3 cam = camera.position();

				double x = pos.x - cam.x;
				double z = pos.z - cam.z;

				if (offset == null)
					offset = new float[3];
				else
					offset = offset.clone();

				offset[1] -= (float) ((x * x + z * z) / Config.worldCurvature);
			}
		}

		return offset;
	}

	@Override
	public @Nullable RenderSectionManager getRenderSectionManager() {
		return renderSectionManager;
	}
}
