package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.sodium.ext.*;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.RenderPhase;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(value = LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(
		method = "renderLevel",
		at = @At(value = "HEAD")
	)
	private void cfi_levelStart(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		RenderPhase.renderingLevel = true;
	}

	@Inject(
		method = "renderLevel",
		at = @At(value = "RETURN")
	)
	private void cfi_levelEnd(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		RenderPhase.renderingLevel = false;
	}

	@Inject(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
			shift = At.Shift.AFTER
		)
	)
	private void modifyRenderLevel(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return;

		SkyFBO fbo = SkyFBO.getInstance();
		if (fbo != null)
			fbo.blitFromTexture(
				Utils.mainColorTexture(),
				Utils.mainTargetWidth(),
				Utils.mainTargetHeight(),
				true
			);

		SodiumWorldRendererExt ext = SodiumWorldRendererExt.Holder.instance;
		if (ext == null)
			return;
		RenderSectionManagerExt manager = ext.getRenderSectionManager();
		if (manager == null)
			return;

		Iterator<ChunkRenderListExt> renderLists = manager.renderLists();
		while (renderLists.hasNext()) {
			ChunkRenderListExt renderList = renderLists.next();

			ByteIteratorExt geometrySections = renderList.getSectionsWithGeometryIterator(false);
			if (geometrySections != null)
				while (geometrySections.next())
					processChunk(renderList.region(), geometrySections.getNext());
		}

		if (CompatibilityHook.isDHRenderingEnabled())
			LodMaskTexture.createAndUpdate();
	}

	private static void processChunk(RenderRegionExt region, int sectionIndex) {
		RenderSectionExt section = region.section(sectionIndex);
		if (section == null) return;

		region.processChunk(section, sectionIndex);
	}
}
