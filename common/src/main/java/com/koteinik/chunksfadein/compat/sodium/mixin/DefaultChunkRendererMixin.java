package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;
import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import org.lwjgl.opengl.GL31;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class DefaultChunkRendererMixin {
	@Unique
	private static int cfi_fadeTex = 0;

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V",
			shift = At.Shift.AFTER,
			ordinal = 0
		)
	)
	private void cfi_bindUniforms(
		CallbackInfo ci,
		@Local(argsOnly = true) RenderPass pass
	) {
		SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
		if (renderer == null)
			return;

		ChunkFadeInController controller = ((SodiumWorldRendererExt) renderer).getChunkFadeInController();
		if (controller == null)
			return;

		if (!CompatibilityHook.isIrisShaderPackInUse()) {
			pass.setUniform("cfi_u_FadeData", controller.getFadeBuffer());
			pass.setUniform("cfi_u_Globals", controller.getUniformBuffer());
			pass.setUniform(
				"cfi_sky",
				SkyFBO.getInstance().texture.getColorTextureView(),
				RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
			);
		} else {
			GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, 7, ((GlBuffer) controller.getUniformBuffer()).handle());

			int handle = ((GlBuffer) controller.getFadeBuffer()).handle();
			if (cfi_fadeTex == 0) cfi_fadeTex = GlStateManager._genTexture();

			GlStateManager._activeTexture(GL31.GL_TEXTURE13);
			GL31.glBindTexture(GL31.GL_TEXTURE_BUFFER, cfi_fadeTex);
			GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, GL31.GL_RGBA32F, handle);
		}
	}
}
