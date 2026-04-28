package com.koteinik.chunksfadein.compat.dh.mixin.iris;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.compat.dh.ext.DhRenderProgramExt;
import com.koteinik.chunksfadein.compat.dh.ext.GlDhMetaRendererExt;
import com.koteinik.chunksfadein.compat.dh.mixin.GlDhTerrainRendererMixin;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShaderProgram;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.common.render.openGl.GlDhMetaRenderer;
import com.seibel.distanthorizons.common.render.openGl.GlDhTerrainRenderer;
import com.seibel.distanthorizons.common.render.openGl.postProcessing.ssao.GlDhSSAOApplyShader;
import com.seibel.distanthorizons.common.render.openGl.postProcessing.ssao.GlDhSSAOShader;
import com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(value = GlDhMetaRenderer.class, remap = false)
public abstract class IrisGlDhMetaRendererMixin implements GlDhMetaRendererExt {
	@Shadow
	private IDhApiShaderProgram shaderProgramForThisFrame;

	@Shadow
	public int getActiveColorTextureId() {
		return 0;
	}

	@Override
	public DhRenderProgramExt getShader() {
		if (shaderProgramForThisFrame instanceof DhRenderProgramExt ext)
			return ext;
		else
			return null;
	}

	@Override
	public void rebuildShaders() {
		GlDhTerrainShaderProgram old = GlDhTerrainRenderer.INSTANCE.getTerrainShaderProgram();
		old.unbind();
		old.free();
		((GlDhTerrainRendererMixin) GlDhTerrainRenderer.INSTANCE)
			.setTerrainShaderProgram(null);

		GlDhSSAOShader.INSTANCE.free();
		GlDhSSAOShader.INSTANCE = new GlDhSSAOShader();
		GlDhSSAOApplyShader.INSTANCE.free();
		GlDhSSAOApplyShader.INSTANCE = new GlDhSSAOApplyShader();
	}

	@Override
	public int activeColorTexture() {
		return getActiveColorTextureId();
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void modifyConstructor(CallbackInfo ci) {
		ShaderUtils.lodRenderer = this;
	}

	@Inject(
		method = "setGLState",
		at = @At(
			value = "INVOKE",
			target = "Lorg/lwjgl/opengl/GL32;glClearDepth(D)V"
		)
	)
	private void avoidClear(DhApiRenderParam renderEventParam, boolean firstPass, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHSSAOEnabled())
			return;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer drawBuffers = stack.mallocInt(1);
			drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0);
			drawBuffers.flip();
			GL20.glDrawBuffers(drawBuffers);
		}
	}
}
