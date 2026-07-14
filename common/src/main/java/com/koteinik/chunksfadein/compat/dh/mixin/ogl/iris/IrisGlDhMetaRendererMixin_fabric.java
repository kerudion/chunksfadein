package com.koteinik.chunksfadein.compat.dh.mixin.ogl.iris;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.compat.dh.ext.DhRenderProgramExt;
import com.koteinik.chunksfadein.compat.dh.ext.GlDhMetaRendererExt;
import com.koteinik.chunksfadein.compat.dh.mixin.ogl.GlDhTerrainRendererMixin_fabric;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShaderProgram;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.common.render.openGl.GlDhMetaRenderer_fabric;
import com.seibel.distanthorizons.common.render.openGl.GlDhTerrainRenderer_fabric;
import com.seibel.distanthorizons.common.render.openGl.postProcessing.ssao.GlDhSSAOApplyShader_fabric;
import com.seibel.distanthorizons.common.render.openGl.postProcessing.ssao.GlDhSSAOShader_fabric;
import com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram_fabric;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(value = GlDhMetaRenderer_fabric.class, remap = false)
public abstract class IrisGlDhMetaRendererMixin_fabric implements GlDhMetaRendererExt {
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
		GlDhTerrainShaderProgram_fabric old = GlDhTerrainRenderer_fabric.INSTANCE.getTerrainShaderProgram();
		old.unbind();
		old.free();
		((GlDhTerrainRendererMixin_fabric) GlDhTerrainRenderer_fabric.INSTANCE)
			.setTerrainShaderProgram(null);

		GlDhSSAOShader_fabric.INSTANCE.free();
		GlDhSSAOShader_fabric.INSTANCE = new GlDhSSAOShader_fabric();
		GlDhSSAOApplyShader_fabric.INSTANCE.free();
		GlDhSSAOApplyShader_fabric.INSTANCE = new GlDhSSAOApplyShader_fabric();
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
			target = "Lorg/lwjgl/opengl/GL33;glClearDepth(D)V"
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
