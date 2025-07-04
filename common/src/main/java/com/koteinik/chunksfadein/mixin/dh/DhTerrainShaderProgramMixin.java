package com.koteinik.chunksfadein.mixin.dh;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.core.dh.LodMaskTexture;
import com.koteinik.chunksfadein.extensions.DhRenderProgramExt;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.api.enums.config.EDhApiMcRenderingFadeMode;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.core.render.glObject.shader.ShaderProgram;
import com.seibel.distanthorizons.core.render.renderer.DhTerrainShaderProgram;
import com.seibel.distanthorizons.core.util.RenderUtil;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = DhTerrainShaderProgram.class, remap = false)
public abstract class DhTerrainShaderProgramMixin extends ShaderProgram implements DhRenderProgramExt {
	@Unique
	private int screenSize;
	@Unique
	private int chunkFadeData;
	@Unique
	private int lodMask;
	@Unique
	private int lodMaskDim;
	@Unique
	private int lodMaskMaxDist;
	@Unique
	private int lodMaskOrigin;
	@Unique
	private int lodMaskMinY;
	@Unique
	private int terrainFadeTexture;
	@Unique
	private int dhFadeActive;
	@Unique
	private int dhStartFadeBlockDistanceSq;

	public DhTerrainShaderProgramMixin() {
		super(null, null, null);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void modifyConstructor(CallbackInfo ci) {
		this.screenSize = tryGetUniformLocation("cfi_screenSize");
		this.chunkFadeData = tryGetUniformLocation("cfi_chunkFadeData");
		this.lodMask = tryGetUniformLocation("cfi_lodMask");
		this.lodMaskDim = tryGetUniformLocation("cfi_lodMaskDim");
		this.lodMaskMaxDist = tryGetUniformLocation("cfi_lodMaskMaxDist");
		this.lodMaskOrigin = tryGetUniformLocation("cfi_lodMaskOrigin");
		this.lodMaskMinY = tryGetUniformLocation("cfi_lodMaskMinY");
		this.terrainFadeTexture = tryGetUniformLocation("cfi_sky");
		this.dhFadeActive = tryGetUniformLocation("cfi_dhFadeActive");
		this.dhStartFadeBlockDistanceSq = tryGetUniformLocation("cfi_dhStartFadeBlockDistanceSq");
	}

	@Override
	public void bindUniforms(float x, float y, float z, float w) {
		if (chunkFadeData != -1)
			GL30.glUniform4f(chunkFadeData, x, y, z, w);
	}

	@Inject(method = "bind", at = @At(value = "TAIL"))
	private void modifyBind(CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		if (lodMask != -1)
			LodMaskTexture.bind(14);

		if (!Config.isFadeEnabled)
			return;

		if (terrainFadeTexture != -1)
			SkyFBO.active(15);
	}

	@Inject(method = "fillUniformData", at = @At(value = "TAIL"))
	private void modifyFillUniformData(DhApiRenderParam renderParameters, CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		if (Config.isFadeEnabled) {
			if (screenSize != -1) GL30.glUniform2f(screenSize, SkyFBO.getWidth(), SkyFBO.getHeight());
			if (terrainFadeTexture != -1) GL30.glUniform1i(terrainFadeTexture, 15);
		}

		if (dhFadeEnabled()) {
			if (dhFadeActive != -1) GL30.glUniform1i(dhFadeActive, 1);

			float dhNearClipDistance = RenderUtil.getNearClipPlaneInBlocksForFading(/* partialTicks is unused there */ 0);
			dhNearClipDistance += 16f;

			float fadeStartDistance = dhNearClipDistance * 1.5f;

			if (dhStartFadeBlockDistanceSq != -1)
				GL30.glUniform1f(dhStartFadeBlockDistanceSq, fadeStartDistance * fadeStartDistance);
		} else {
			if (dhFadeActive != -1) GL30.glUniform1i(dhFadeActive, 0);
			if (dhStartFadeBlockDistanceSq != -1) GL30.glUniform1f(dhStartFadeBlockDistanceSq, 0);
		}

		LodMaskTexture texture = LodMaskTexture.getInstance();
		if (texture != null) {
			if (lodMask != -1)
				GL30.glUniform1i(lodMask, 14);
			if (lodMaskDim != -1)
				GL30.glUniform3f(
					lodMaskDim,
					texture.sizeX, texture.sizeY, texture.sizeZ
				);
			if (lodMaskMaxDist != -1)
				GL30.glUniform3f(
					lodMaskMaxDist,
					(float) texture.sizeX * 8 + 16,
					(float) texture.sizeY * 8 + 16,
					(float) texture.sizeZ * 8 + 16
				);
			if (lodMaskOrigin != -1) {
				Vec3 cameraPos = Utils.cameraPosition();
				GL30.glUniform3f(
					lodMaskOrigin,
					(float) Math.floor(cameraPos.x / 16),
					(float) Math.floor(cameraPos.y / 16),
					(float) Math.floor(cameraPos.z / 16)
				);
			}
			if (lodMaskMinY != -1)
				GL30.glUniform1f(
					lodMaskMinY,
					texture.minY
				);
		}
	}

	@Inject(method = "unbind", at = @At(value = "TAIL"))
	private void modifyUnbind(CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if (Config.isFadeEnabled)
			GL13.glBindTexture(GL13.GL_TEXTURE_2D, 0);
		GL13.glBindTexture(GL13.GL_TEXTURE_3D, 0);
	}

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/shader/ShaderProgram;<init>(Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/lang/String;[Ljava/lang/String;)V"), index = 0)
	private static Supplier<String> modifySourceVert(Supplier<String> source) {
		return () -> {
			String sauce = source.get();
			if (!Config.isModEnabled) return sauce;
			return prepareVertexInjector().get(sauce);
		};
	}

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/shader/ShaderProgram;<init>(Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/lang/String;[Ljava/lang/String;)V"), index = 1)
	private static Supplier<String> modifySourceFrag(Supplier<String> source) {
		return () -> {
			String sauce = source.get();
			if (!Config.isModEnabled) return sauce;
			return prepareFragmentInjector().get(sauce);
		};
	}

	@Unique
	private static ShaderInjector prepareVertexInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.insertAfterInVars("in vec4 irisExtra;");

		injector.insertAfterOutVars(shader
			                            .newLine("uniform vec4 cfi_chunkFadeData;")
			                            .newLine("uniform vec3 cfi_lodMaskOrigin;")
			                            .vertOutVars()
			                            .flushMultiline());

		injector.insertAfterStr(
			"vertexWorldPos = ",
			shader
				.newLine("vec4 chunkFadeData = cfi_chunkFadeData;")
				.newLine("vec3 localPos = vec3(vPosition.xyz);")
				.newLine("vec3 offsetPos = floor((vertexWorldPos - mod(localPos, 16.0)) / 16.0) + cfi_lodMaskOrigin;")
				.vertInitOutVars("localPos", "offsetPos")
				.vertInitMod("localPos", "vertexWorldPos", false, "offsetPos", true)
				// push water and lava slightly down
				.newLine("if (irisExtra.x == 12.0 || irisExtra.x == 6.0) { vertexWorldPos.y -= 0.115; }")
				.flushMultiline()
		);

		return injector;
	}

	@Unique
	private static ShaderInjector prepareFragmentInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		if (Config.isFadeEnabled) {
			injector.replace("#version 150", "#version 330 core");

			injector.replace(
				"out vec4 fragColor;",
				"layout(location = 0) out vec4 fragColor;",
				"layout(location = 1) out vec4 cfi_terrainFadeOut;"
			);
		}

		injector.insertAfterInVars(shader.fragInVars()
		                                 .flushMultiline());

		shader.fragColorMod("fragColor.rgb", false);

		if (Config.isFadeEnabled && CompatibilityHook.isDHSSAOEnabled())
			shader.newLine("cfi_terrainFadeOut.a = fade;");

		if (Config.isFadeEnabled)
			shader.newLine("bool shouldDiscardFrag = false;");

		injector.insertAfterStr(
			"fragColor = vertexColor;",
			shader.flushMultiline()
		);

		injector.insertAfterInVars(
			"uniform sampler3D cfi_lodMask;",
			"uniform vec3 cfi_lodMaskDim;",
			"uniform vec3 cfi_lodMaskMaxDist;",
			"uniform vec3 cfi_lodMaskOrigin;",
			"uniform float cfi_lodMaskMinY;",
			"uniform bool cfi_dhFadeActive;",
			"uniform float cfi_dhStartFadeBlockDistanceSq;"
		);

		shader.dhMaskLod("discard;", "vPos", "vertexWorldPos", true);

		if (Config.isFadeEnabled) {
			shader.newLine("if (fragColor.a < 1.0) {");
			shader.newLine(
				"vec3 destinationColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;");
			shader.newLine("cfi_terrainFadeOut.rgb = mix(destinationColor, fragColor.rgb, fragColor.a);");
			shader.newLine("} else { cfi_terrainFadeOut.rgb = fragColor.rgb; }");
		}

		injector.appendToFunction(
			"main",
			shader.flushMultiline()
		);

		// probably should do something better with these...
		injector.replace("viewDist < uClipDistance && uClipDistance > 0.0", "false");
		injector.replace("if (uDitherDhRendering)", "if (false)");

		return injector;
	}

	private static boolean dhFadeEnabled() {
		return com.seibel.distanthorizons.core.config.Config.Client.Advanced.Graphics.Quality.vanillaFadeMode.get() != EDhApiMcRenderingFadeMode.NONE;
	}
}
