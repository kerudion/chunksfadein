package com.koteinik.chunksfadein.core;

import java.util.ArrayList;
import java.util.List;

import static com.koteinik.chunksfadein.config.Config.*;
import static com.koteinik.chunksfadein.core.AnimationType.DISPLACEMENT;
import static com.koteinik.chunksfadein.core.AnimationType.JAGGED;
import static com.koteinik.chunksfadein.core.FadeType.*;

public class FadeShader {
	private List<String> lines = new ArrayList<>();

	public FadeShader dummyApiFragSampleSkyLodTexture() {
		if (!isModEnabled)
			return this;

		return newLine("vec3 cfi_sampleSkyLodTexture() { return vec3(0.0); }");
	}

	public FadeShader apiFragSampleSkyLodTexture() {
		if (!isModEnabled)
			return this;

		newLine("vec3 cfi_sampleSkyLodTexture() {");
		newLine("return texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;");
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

		if (isFadeEnabled)
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

		if (isFadeEnabled)
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

		if (isFadeEnabled)
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

		if (isFadeEnabled) {
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

		if (isCurvatureEnabled)
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

		if (isAnimationEnabled) {
			newLine("vec3 originalPos = localPos;");
			newLine("localPos = vec3(localPos);");

			newLine("vec4 chunkFadeData = cfi_getFadeData();");

			if (animationType == JAGGED || animationType == DISPLACEMENT)
				rand("rand", "localPos + vec3(_draw_id)");

			calculateVertexDisplacement("localPos", null, true, "_draw_id");

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
		if (!isModEnabled || !isFadeEnabled)
			return this;

		newLine("flat out float cfi_FadeFactor;");

		if (fadeType == BLOCK)
			newLine("out vec3 cfi_BlockSeed;");
		if (fadeType == LINED)
			newLine("out float cfi_RefFactor;");
		if (fadeType == VERTEX)
			newLine("flat out float cfi_RefFactor;");

		return this;
	}

	public FadeShader fragInVars() {
		if (!isModEnabled || !isFadeEnabled)
			return this;

		newLine("uniform sampler2D cfi_sky;");
		newLine("uniform vec2 cfi_screenSize;");

		newLine("flat in float cfi_FadeFactor;");

		if (fadeType == BLOCK)
			newLine("in vec3 cfi_BlockSeed;");
		if (fadeType == LINED)
			newLine("in float cfi_RefFactor;");
		if (fadeType == VERTEX)
			newLine("flat in float cfi_RefFactor;");

		return this;
	}

	public FadeShader vertInitOutVarsDrawId(String localPos, String drawId) {
		if (!isModEnabled)
			return this;

		if (isAnimationEnabled || isFadeEnabled)
			newLine("vec4 chunkFadeData = cfi_ChunkFadeDatas[%s].fadeData;".formatted(drawId));

		return vertInitOutVars(localPos, "vec3(%s)".formatted(drawId));
	}

	public FadeShader vertInitOutVars(String localPos, String randSeed) {
		if (!isModEnabled)
			return this;

		if (isAnimationEnabled || isFadeEnabled)
			if (animationType == JAGGED || animationType == DISPLACEMENT || fadeType == VERTEX)
				rand("rand", "%s + %s".formatted(localPos, randSeed));

		if (isFadeEnabled) {
			newLine("cfi_FadeFactor = chunkFadeData.w;");

			if (fadeType == BLOCK)
				newLine("cfi_BlockSeed = %s + %s;".formatted(localPos, randSeed));
			if (fadeType == VERTEX)
				newLine("cfi_RefFactor = cfi_FadeFactor > rand ? 1.0 : cfi_FadeFactor / rand;");
			if (fadeType == LINED) {
				newLine("float refFactor = %s.y / 16.0;".formatted(localPos));
				newLine("cfi_RefFactor = refFactor - floor(refFactor);");
			}
		}

		return this;
	}

	public FadeShader vertInitMod(String localPos, String position, boolean modifyLocal, String randSeed, boolean addCurvature) {
		if (!isModEnabled)
			return this;

		if (isAnimationEnabled)
			calculateVertexDisplacement(localPos, position, modifyLocal, randSeed);

		if (addCurvature && isCurvatureEnabled)
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

	public FadeShader fragColorMod(String color, String fadeColor, boolean addIf) {
		if (!isModEnabled || !isFadeEnabled)
			return this;

		if (addIf)
			newLine("if (cfi_FadeFactor < 1.0) {");

		calculateFade("float fade = ");
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
				randAppend("rand2", "%s - %s".formatted(localPos, randSeed));
				randAppend("rand3", "%s + (%s * 2)".formatted(localPos, randSeed));
				append("%s += vec3(rand - 0.5, rand2 - 0.5, rand3 - 0.5) * vec3(chunkFadeData.y);".formatted(modifyLocal ? localPos : position));
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
			newLine(prefix + "cfi_FadeFactor;");
		if (fadeType == LINED)
			newLine(prefix + "cfi_RefFactor <= cfi_FadeFactor ? 1.0 : 0.0;");
		if (fadeType == BLOCK) {
			rand("rand", "floor(cfi_BlockSeed)");
			newLine(prefix + "cfi_FadeFactor > rand ? 1.0 : cfi_FadeFactor / rand;");
		}
		if (fadeType == VERTEX)
			newLine(prefix + "cfi_RefFactor;");

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

		if (isAnimationEnabled) {
			newLine("vec3 originalPos = localPos;");

			newLine("localPos = vec3(localPos);");

			newLine("vec4 chunkFadeData = cfi_getFadeData();");

			if (animationType == JAGGED || animationType == DISPLACEMENT)
				rand("rand", "worldPos");

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
		if (isAnimationEnabled || isFadeEnabled)
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

	public FadeShader rand(String name, String vector) {
		newLine("float %s = fract(sin(dot(%s, vec3(12.9898, 78.233, 132.383))) * 43758.5453);"
			        .formatted(name, vector));
		newLine("if (%s == 0.0) %s = 0.001;".formatted(name, name));

		return this;
	}

	public void randAppend(String name, String vector) {
		append("float %s = fract(sin(dot(%s, vec3(12.9898, 78.233, 132.383))) * 43758.5453);"
			       .formatted(name, vector));
		append("if (%s == 0.0) %s = 0.001;".formatted(name, name));
	}

	public FadeShader newLine(String line) {
		lines.add(line);

		return this;
	}

	public FadeShader append(String value) {
		int last = lines.size() - 1;
		lines.set(last, lines.get(last) + value);

		return this;
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
