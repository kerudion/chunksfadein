package com.koteinik.chunksfadein.core;

import java.util.ArrayList;
import java.util.List;

import static com.koteinik.chunksfadein.config.Config.*;
import static com.koteinik.chunksfadein.core.AnimationType.DISPLACEMENT;
import static com.koteinik.chunksfadein.core.AnimationType.JAGGED;
import static com.koteinik.chunksfadein.core.FadeType.*;

public class FadeShader {
	private List<String> lines = new ArrayList<>();

	private String inPrefix = "";
	private String outPrefix = "";

	private boolean animation = isAnimationEnabled;
	private boolean fade = isFadeEnabled;
	private boolean curvature = isCurvatureEnabled;

	public FadeShader overrideAnimation(boolean value) {
		animation = value;

		return this;
	}

	public FadeShader overrideFade(boolean value) {
		fade = value;

		return this;
	}

	public FadeShader overrideCurvature(boolean value) {
		curvature = value;

		return this;
	}

	public FadeShader inPrefix(String value) {
		inPrefix = value;

		return this;
	}

	public FadeShader outPrefix(String value) {
		outPrefix = value;

		return this;
	}

	public FadeShader dummyApiFragSampleSkyLodTexture() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_sampleSkyLodTexture() { return vec3(0.0); }");
	}

	public FadeShader apiFragSampleSkyLodTexture() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_sampleSkyLodTexture() {");
		if (fade)
			newLine("return texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;");
		else
			newLine("return iris_FogColor.rgb;");
		newLine("}");

		return this;
	}

	public FadeShader dummyApiFragApplySkyLodFade() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_applySkyLodFade(vec3 fullyFaded) { return vec3(0.0); }");
	}

	public FadeShader apiFragApplySkyLodFade() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_applySkyLodFade(vec3 fullyFaded) {");

		if (fade)
			newLine("return cfi_applyFade(cfi_sampleSkyLodTexture(), fullyFaded);");
		else
			newLine("return fullyFaded;");

		newLine("}");

		return this;
	}

	public FadeShader dummyApiFragApplyFogFade() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_applyFogFade(vec3 fullyFaded) { return vec3(0.0); }");
	}

	public FadeShader apiFragApplyFogFade() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_applyFogFade(vec3 fullyFaded) {");

		if (fade)
			newLine("return cfi_applyFade(iris_FogColor.rgb, fullyFaded);");
		else
			newLine("return fullyFaded;");

		newLine("}");

		return this;
	}

	public FadeShader dummyApiFragApplyFade() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_applyFade(vec3 fullyUnfaded, vec3 fullyFaded) { return vec3(0.0); }");
	}

	public FadeShader apiFragApplyFade() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_applyFade(vec3 fullyUnfaded, vec3 fullyFaded) {");

		if (fade)
			newLine("return mix(fullyUnfaded, fullyFaded, cfi_calculateFade());");
		else
			newLine("return fullyFaded;");

		newLine("}");

		return this;
	}

	public FadeShader dummyApiFragCalculateFade() {
		if (!isModEnabled)
			return this;

		return newLine("float cfi_calculateFade() { return 0.0; }");
	}

	public FadeShader apiFragCalculateFade() {
		if (!isModEnabled)
			return this;

		newLine("float cfi_calculateFade() {");

		if (fade) {
			newLine("float fade = 0.0;");
			calculateFade("fade = ");
			newLine("return fade;");
		} else {
			newLine("return 1.0;");
		}

		newLine("}");

		return this;
	}


	public FadeShader dummyApiVertCalculateCurvature2() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_calculateCurvature() { return vec3(0.0); }");
	}

	public FadeShader apiVertCalculateCurvature2() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_calculateCurvature() {");
		newLine("return cfi_calculateCurvature(getVertexPosition().xyz);");
		newLine("}");

		return this;
	}


	public FadeShader dummyApiVertCalculateCurvature() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_calculateCurvature(vec3 globalPos) { return vec3(0.0); }");
	}

	public FadeShader apiVertCalculateCurvature() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_calculateCurvature(vec3 globalPos) {");

		if (curvature)
			newLine("return vec3(0.0, -dot(globalPos, globalPos) / " + worldCurvature + ", 0.0);");
		else
			newLine("return vec3(0.0);");

		newLine("}");

		return this;
	}


	public FadeShader dummyApiVertCalculateDisplacement2() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_calculateDisplacement() { return vec3(0.0); }");
	}

	public FadeShader apiVertCalculateDisplacement2() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_calculateDisplacement() {");
		newLine("return cfi_calculateDisplacement(_vert_position);");
		newLine("}");

		return this;
	}

	public FadeShader dummyApiVertCalculateDisplacement() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_calculateDisplacement(vec3 localPos) { return vec3(0.0); }");
	}

	public FadeShader apiVertCalculateDisplacement() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_calculateDisplacement(vec3 localPos) {");

		if (animation) {
			newLine("vec3 originalPos = localPos;");
			newLine("localPos = vec3(localPos);");

			newLine("vec4 chunkFadeData = cfi_getFadeData();");

			if (animationType == JAGGED || animationType == DISPLACEMENT)
				newLine("float rand = _cfi_rand(localPos + vec3(_draw_id));");

			calculateVertexDisplacement("localPos", null, true, "int(_draw_id)");

			newLine("return localPos - originalPos;");
		} else {
			newLine("return vec3(0.0);");
		}

		newLine("}");

		return this;
	}

	public FadeShader dummyApiVertGetFadeData() {
		if (!isModEnabled)
			return this;

		return newLine("vec4 cfi_getFadeData() { return vec4(0.0); }");
	}

	public FadeShader apiVertGetFadeData(String drawId) {
		if (!isModEnabled)
			return this;

		newLine("vec4 cfi_getFadeData() {");
		newLine("return cfi_ChunkFadeDatas[%s].fadeData;".formatted(drawId));
		newLine("}");

		return this;
	}

	public FadeShader vertInVars() {
		newLine("struct cfi_ChunkFadeData { vec4 fadeData; };");
		newLine("layout(std140) uniform cfi_ubo_ChunkFadeDatas { cfi_ChunkFadeData cfi_ChunkFadeDatas[256]; };");

		return this;
	}

	public FadeShader vertOutVars() {
		if (!isModEnabled || !fade)
			return this;

		insertVars(
			"out",
			"flat out",
			"{out}",
			""
		);

		return this;
	}

	public FadeShader geomVars() {
		if (!isModEnabled || !fade)
			return this;

		insertVars(
			"in",
			"flat in",
			"{in}",
			"[]"
		);

		insertVars(
			"out",
			"flat out",
			"{out}",
			""
		);

		if (fadeType != FULL)
			newLine("int cfi_counter;");

		return this;
	}

	public FadeShader geomMainHead() {
		if (!isModEnabled || !fade)
			return this;

		if (fadeType != FULL)
			newLine("cfi_counter = 0;");

		newLine("{out}cfi_FadeFactor = {in}cfi_FadeFactor[0];");

		return this;
	}

	public FadeShader geomProxyVars() {
		if (!isModEnabled || !fade)
			return this;

		if (fadeType != FULL) {
			newLine("cfi_counter++;");
			newLine("cfi_counter = int(mod(cfi_counter, gl_in.length()));");
		}

		if (fadeType == BLOCK || fadeType == FRAGMENTED)
			newLine("{out}cfi_BlockSeed = {in}cfi_BlockSeed[cfi_counter];");
		if (fadeType == LINED)
			newLine("{out}cfi_RefFactor = {in}cfi_RefFactor[cfi_counter];");
		if (fadeType == VERTEX)
			newLine("{out}cfi_RefFactor = {in}cfi_RefFactor[cfi_counter];");

		return this;
	}

	public FadeShader tessControlVars() {
		if (!isModEnabled || !fade)
			return this;

		insertVars(
			"in",
			"flat in",
			"{in}",
			"[]"
		);

		insertVars(
			"patch out",
			"patch out",
			"{out}",
			""
		);

		return this;
	}

	public FadeShader tessControlProxyVars() {
		if (!isModEnabled || !fade)
			return this;

		newLine("{out}cfi_FadeFactor = {in}cfi_FadeFactor[0];");

		if (fadeType == BLOCK || fadeType == FRAGMENTED)
			newLine("{out}cfi_BlockSeed = {in}cfi_BlockSeed[0];");
		if (fadeType == LINED)
			newLine("{out}cfi_RefFactor = {in}cfi_RefFactor[0];");
		if (fadeType == VERTEX)
			newLine("{out}cfi_RefFactor = {in}cfi_RefFactor[0];");

		return this;
	}

	public FadeShader tessEvalVars(boolean hasTessControl) {
		if (!isModEnabled || !fade)
			return this;

		if (hasTessControl)
			insertVars(
				"patch in",
				"patch in",
				"{in}",
				""
			);
		else
			insertVars(
				"in",
				"flat in",
				"{in}",
				"[]"
			);

		insertVars(
			"out",
			"flat out",
			"{out}",
			""
		);

		return this;
	}

	public FadeShader tessEvalProxyVars(boolean hasTessControl) {
		if (!isModEnabled || !fade)
			return this;

		if (hasTessControl) {
			newLine("{out}cfi_FadeFactor = {in}cfi_FadeFactor;");

			if (fadeType == BLOCK || fadeType == FRAGMENTED)
				newLine("{out}cfi_BlockSeed = {in}cfi_BlockSeed;");
			if (fadeType == LINED)
				newLine("{out}cfi_RefFactor = {in}cfi_RefFactor;");
			if (fadeType == VERTEX)
				newLine("{out}cfi_RefFactor = {in}cfi_RefFactor;");
		} else {
			newLine("{out}cfi_FadeFactor = {in}cfi_FadeFactor[0];");

			if (fadeType == BLOCK || fadeType == FRAGMENTED)
				newLine("{out}cfi_BlockSeed = {in}cfi_BlockSeed[0];");
			if (fadeType == LINED)
				newLine("{out}cfi_RefFactor = {in}cfi_RefFactor[0];");
			if (fadeType == VERTEX)
				newLine("{out}cfi_RefFactor = {in}cfi_RefFactor[0];");
		}

		return this;
	}

	public FadeShader fragInVars() {
		if (!isModEnabled || !fade)
			return this;

		newLine("uniform sampler2D cfi_sky;");
		newLine("uniform vec2 cfi_screenSize;");

		insertVars(
			"in",
			"flat in",
			"{in}",
			""
		);

		return this;
	}

	private FadeShader insertVars(String mods, String flatMods, String prefix, String suffix) {
		newLine("%s float %scfi_FadeFactor%s;".formatted(flatMods, prefix, suffix));

		if (fadeType == BLOCK || fadeType == FRAGMENTED)
			newLine("%s vec3 %scfi_BlockSeed%s;".formatted(mods, prefix, suffix));
		if (fadeType == LINED)
			newLine("%s float %scfi_RefFactor%s;".formatted(mods, prefix, suffix));
		if (fadeType == VERTEX)
			newLine("%s float %scfi_RefFactor%s;".formatted(flatMods, prefix, suffix));

		return this;
	}

	public FadeShader vertInitOutVarsDrawId(String localPos, String drawId) {
		if (!isModEnabled)
			return this;

		if (animation || fade)
			newLine("vec4 chunkFadeData = cfi_ChunkFadeDatas[%s].fadeData;".formatted(drawId));

		return vertInitOutVars(localPos, "vec3(%s)".formatted(drawId));
	}

	public FadeShader vertInitOutVars(String localPos, String randSeed) {
		if (!isModEnabled)
			return this;

		if (animation || fade)
			if (animationType == JAGGED || animationType == DISPLACEMENT || fadeType == VERTEX)
				newLine("float rand = _cfi_rand(%s + %s);".formatted(localPos, randSeed));

		if (fade) {
			newLine("{out}cfi_FadeFactor = chunkFadeData.w;");

			if (fadeType == BLOCK || fadeType == FRAGMENTED)
				newLine("{out}cfi_BlockSeed = %s + %s;".formatted(localPos, randSeed));
			if (fadeType == VERTEX)
				newLine("{out}cfi_RefFactor = {out}cfi_FadeFactor > rand ? 1.0 : {out}cfi_FadeFactor / rand;");
			if (fadeType == LINED) {
				newLine("float refFactor = %s.y / 17.0;".formatted(localPos));
				newLine("{out}cfi_RefFactor = refFactor - floor(refFactor);");
			}
		}

		return this;
	}

	public FadeShader vertInitMod(String localPos, String position, boolean modifyLocal, String randSeed, boolean addCurvature) {
		if (!isModEnabled)
			return this;

		if (animation)
			calculateVertexDisplacement(localPos, position, modifyLocal, randSeed);

		if (addCurvature && curvature)
			newLine("%s.y -= dot(%s, %s) / %s;".formatted(
				modifyLocal ? localPos : position,
				position,
				position,
				worldCurvature
			));

		return this;
	}

	public FadeShader fragColorMod(String color) {
		return fragColorMod(color, true);
	}

	public FadeShader fragColorMod(String color, boolean addIf) {
		return fragColorMod(color, "texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb", addIf);
	}

	public FadeShader fragColorMod(String color, String fadeColor) {
		return fragColorMod(color, fadeColor, true);
	}

	public FadeShader fragColorMod(String color, String fadeColor, boolean addIf) {
		if (!isModEnabled || !fade)
			return this;

		if (addIf)
			newLine("if ({in}cfi_FadeFactor < 1.0) {");

		calculateFade("float fade = ");
		if (fadeMixType == FadeMixType.OKLAB)
			newLine("%s = _cfi_mix_srgb_in_oklab(%s, %s, fade);"
				.formatted(color, fadeColor, color));
		else
			newLine("%s = mix(%s, %s, fade);"
				.formatted(color, fadeColor, color));

		if (addIf)
			newLine("}");

		return this;
	}

	public FadeShader calculateVertexDisplacement(String localPos, String position, boolean modifyLocal, String randSeed) {
		switch (animationType) {
			case FULL:
			case JAGGED:
				newLine("%s += chunkFadeData.xyz".formatted(modifyLocal ? localPos : position));
				if (animationType == JAGGED)
					append(" * rand");
				append(";");
				break;
			case DISPLACEMENT:
				newLine(
					"if (%s.x != 0.0 && %s.y != 0.0 && %s.z != 0.0 && %s.x != 16.0 && %s.y != 16.0 && %s.z != 16.0) {"
						.replace("%s", localPos));
				append("float rand2 = _cfi_rand(%s - %s);".formatted(localPos, randSeed));
				append("float rand3 = _cfi_rand(%s + (%s * 2));".formatted(localPos, randSeed));
				append("%s += vec3(rand - 0.5, rand2 - 0.5, rand3 - 0.5) * vec3(chunkFadeData.y);".formatted(modifyLocal
					? localPos
					: position));
				append("}");
				break;
			case SCALE:
				if (modifyLocal)
					newLine("%s = mix(vec3(8.0), %s, 1.0 - chunkFadeData.y);"
						.formatted(localPos, localPos));
				else
					newLine("%s += vec3(8.0) - mix(vec3(8.0), %s, chunkFadeData.y);"
						.formatted(position, localPos));
				break;
		}

		return this;
	}

	public FadeShader calculateFade(String prefix) {
		if (fadeType == FadeType.FULL)
			newLine(prefix + "{in}cfi_FadeFactor;");
		if (fadeType == LINED)
			newLine(prefix + "{in}cfi_RefFactor <= {in}cfi_FadeFactor ? 1.0 : 0.0;");
		if (fadeType == BLOCK) {
			newLine("float rand = _cfi_rand(floor({in}cfi_BlockSeed));");
			newLine(prefix + "{in}cfi_FadeFactor > rand ? 1.0 : {in}cfi_FadeFactor / rand;");
		}
		if (fadeType == VERTEX)
			newLine(prefix + "{in}cfi_RefFactor;");
		if (fadeType == FRAGMENTED) {
			newLine("float rand = _cfi_rand(floor({in}cfi_BlockSeed));");

			newLine("float start = rand * (1.0 - 0.2);");
			newLine(prefix + "smoothstep(start, start + 0.2, {in}cfi_FadeFactor);");
		}

		return this;
	}

	public FadeShader dhApiGetFadeData() {
		if (!isModEnabled)
			return this;

		newLine("vec4 cfi_getFadeData() {");
		newLine("return cfi_chunkFadeData;");
		newLine("}");

		return this;
	}

	public FadeShader dhApiLodIsMasked2() {
		if (!isModEnabled)
			return this;

		newLine("bool cfi_dhLodIsMasked() {");
		newLine("return cfi_dhLodIsMasked(cfi_localPos, cfi_worldPos);");
		newLine("}");

		return this;
	}

	public FadeShader dhApiLodIsMasked() {
		if (!isModEnabled)
			return this;

		newLine("bool cfi_dhLodIsMasked(vec3 localPos, vec3 worldPos) {");

		dhMaskLod("return true;", "localPos", "worldPos", false);

		newLine("return false;");
		newLine("}");

		return this;
	}

	public FadeShader dhApiVertCalculateDisplacement2() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_calculateDisplacement() {");
		newLine("return cfi_calculateDisplacement(_vert_position, _cfi_worldPos);");
		newLine("}");

		return this;
	}

	public FadeShader dhApiVertCalculateDisplacement() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_calculateDisplacement(vec3 localPos, vec3 worldPos) {");

		if (animation) {
			newLine("vec3 originalPos = localPos;");

			newLine("localPos = vec3(localPos);");

			newLine("vec4 chunkFadeData = cfi_getFadeData();");

			if (animationType == JAGGED || animationType == DISPLACEMENT)
				newLine("float rand = _cfi_rand(worldPos);");

			newLine("vec3 offsetPos = floor((worldPos - mod(localPos, 16.0)) / 16.0) + cfi_lodMaskOrigin;");

			calculateVertexDisplacement("localPos", null, true, "offsetPos");

			newLine("return localPos - originalPos;");
		} else {
			newLine("return vec3(0.0);");
		}

		newLine("}");

		return this;
	}

	public FadeShader dhApiVertInitOutVars2() {
		if (!isModEnabled)
			return this;

		newLine("void cfi_initOutVars() {");
		newLine("cfi_initOutVars(_vert_position, cfi_worldPos);");
		newLine("}");

		return this;
	}

	public FadeShader dhApiVertInitOutVars() {
		if (!isModEnabled)
			return this;

		newLine("void cfi_initOutVars(vec3 localPos, vec3 worldPos) {");
		if (animation || fade)
			newLine("vec4 chunkFadeData = cfi_getFadeData();");
		newLine("vec3 offsetPos = floor((worldPos - mod(localPos, 16.0)) / 16.0) + cfi_lodMaskOrigin;");
		vertInitOutVars("localPos", "offsetPos");
		newLine("}");

		return this;
	}

	public FadeShader dhMaskLod(String whenOccluded, String vertexPos, String vertexWorldPos, boolean addDhFadeCheck) {
		newLine("vec2 absWorldPosXZ = abs(%s.xz);".formatted(vertexWorldPos));
		newLine("if (absWorldPosXZ.x < cfi_lodMaskMaxDist.x &&" +
			"absWorldPosXZ.y < cfi_lodMaskMaxDist.z" +
			(addDhFadeCheck ? " && dot(%s, %s) < cfi_dhStartFadeBlockDistanceSq".formatted(
				vertexWorldPos,
				vertexWorldPos
			) : "") +
			") {");
		newLine("vec3 offsetPos = %s - mod(%s.xyz + sign(%s) * 0.01, 16.0);".formatted(
			vertexWorldPos,
			vertexPos,
			vertexWorldPos
		));
		newLine("vec3 offsetChunkPos = floor(offsetPos / 16.0) + vec3(1.0);");
		newLine("vec2 texChunkXZ = offsetChunkPos.xz + floor(cfi_lodMaskDim.xz / 2.0);");
		newLine("float texChunkY = offsetChunkPos.y + cfi_lodMaskOrigin.y - cfi_lodMaskMinY;");
		newLine("vec3 uvw = (vec3(texChunkXZ.x, texChunkY, texChunkXZ.y) + vec3(0.5)) / cfi_lodMaskDim;");
		newLine("if (texture(cfi_lodMask, uvw).r == 1.0) { %s }".formatted(whenOccluded));
		//		newLine("if (uvw.x > 0.0 && uvw.x < 1.0 && uvw.y > 0.0 && uvw.y < 1.0 && uvw.z > 0.0 && uvw.z < 1.0) { fragColor.r = 1.0; }");
		//		newLine("if (texture(cfi_lodMask, uvw).r != 0.0) { fragColor.g = texture(cfi_lodMask, uvw).r; }");
		newLine("}");

		return this;
	}

	public FadeShader dhVertOutVars() {
		vertOutVars();

		newLine("out vec3 cfi_localPos;");
		newLine("out vec3 cfi_worldPos;");

		return this;
	}

	public FadeShader dhFragInVars() {
		fragInVars();

		newLine("in vec3 cfi_localPos;");
		newLine("in vec3 cfi_worldPos;");

		return this;
	}

	public FadeShader dhUniforms() {
		newLine("uniform vec4 cfi_chunkFadeData;");
		newLine("uniform sampler3D cfi_lodMask;");
		newLine("uniform vec3 cfi_lodMaskDim;");
		newLine("uniform vec3 cfi_lodMaskMaxDist;");
		newLine("uniform vec3 cfi_lodMaskOrigin;");
		newLine("uniform float cfi_lodMaskMinY;");

		return this;
	}

	public FadeShader utilFunctions() {
		utilRand();
		utilSrgbToOklab();
		utilOklabToSrgb();
		utilMixSrgbInOklab();

		return this;
	}

	public FadeShader utilRand() {
		newLine("float _cfi_rand(vec3 vec) {");
		newLine("    float value = fract(sin(dot(vec, vec3(12.9898, 78.233, 132.383))) * 43758.5453);");
		newLine("    if (value == 0.0) value = 0.001;");
		newLine("    return value;");
		newLine("}");

		return this;
	}

	public FadeShader utilSrgbToOklab() {
		newLine("vec3 _cfi_srgb_to_oklab(vec3 c) {");
		newLine("    vec3 lin = c * c;");
		newLine("    float l = 0.4122214708 * lin.r + 0.5363325363 * lin.g + 0.0514459929 * lin.b;");
		newLine("    float m = 0.2119034982 * lin.r + 0.6806995451 * lin.g + 0.1073969566 * lin.b;");
		newLine("    float s = 0.0883024619 * lin.r + 0.2817188376 * lin.g + 0.6299787005 * lin.b;");
		newLine("    float l_ = pow(l, 0.3333333333);");
		newLine("    float m_ = pow(m, 0.3333333333);");
		newLine("    float s_ = pow(s, 0.3333333333);");
		newLine("    return vec3(");
		newLine("        0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,");
		newLine("        1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,");
		newLine("        0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_");
		newLine("    );");
		newLine("}");

		return this;
	}

	public FadeShader utilOklabToSrgb() {
		newLine("vec3 _cfi_oklab_to_srgb(vec3 c) {");
		newLine("    float l_ = c.x + 0.3963377774 * c.y + 0.2158037573 * c.z;");
		newLine("    float m_ = c.x - 0.1055613458 * c.y - 0.0638541728 * c.z;");
		newLine("    float s_ = c.x - 0.0894841775 * c.y - 1.2914855480 * c.z;");
		newLine("    float l = l_ * l_ * l_;");
		newLine("    float m = m_ * m_ * m_;");
		newLine("    float s = s_ * s_ * s_;");
		newLine("    return sqrt(vec3(");
		newLine("        4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,");
		newLine("        -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,");
		newLine("        -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s");
		newLine("    ));");
		newLine("}");

		return this;
	}

	public FadeShader utilMixSrgbInOklab() {
		newLine("vec3 _cfi_mix_srgb_in_oklab(vec3 a, vec3 b, float t) {");
		newLine("    vec3 labA = _cfi_srgb_to_oklab(a);");
		newLine("    vec3 labB = _cfi_srgb_to_oklab(b);");
		newLine("    vec3 mixedLab = mix(labA, labB, t);");
		newLine("    return _cfi_oklab_to_srgb(mixedLab);");
		newLine("}");

		return this;
	}

	public FadeShader newLine(String line) {
		lines.add(parseLine(line));

		return this;
	}

	public FadeShader newLineIf(boolean condition, String line) {
		if (condition)
			lines.add(parseLine(line));

		return this;
	}

	public FadeShader append(String value) {
		int last = lines.size() - 1;
		lines.set(last, parseLine(lines.get(last) + value));

		return this;
	}

	private String parseLine(String line) {
		return line.replace("{in}", inPrefix)
			.replace("{out}", outPrefix);
	}

	public String flushMultiline() {
		return String.join("\n", flushList());
	}

	public String flushSingleLine() {
		return String.join(" ", flushList());
	}

	public String[] flushArray() {
		return flushList().toArray(String[]::new);
	}

	public List<String> flushList() {
		List<String> lines = this.lines;
		this.lines = new ArrayList<>();

		return lines;
	}
}
