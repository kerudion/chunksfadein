package com.koteinik.chunksfadein.core;

import static com.koteinik.chunksfadein.config.Config.*;
import static com.koteinik.chunksfadein.core.FadeType.*;
import static com.koteinik.chunksfadein.core.AnimationType.*;

import java.util.ArrayList;
import java.util.List;

public class FadeShader {
	private List<String> lines = new ArrayList<>();

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

		newLine("flat in float cfi_FadeFactor;");

		if (fadeType == BLOCK)
			newLine("in vec3 cfi_BlockSeed;");
		if (fadeType == LINED)
			newLine("in float cfi_RefFactor;");
		if (fadeType == VERTEX)
			newLine("flat in float cfi_RefFactor;");

		return this;
	}

	public FadeShader vertInitOutVars(String localPos, String drawId) {
		if (!isModEnabled)
			return this;

		if (isAnimationEnabled || isFadeEnabled) {
			newLine("vec4 chunkFadeData = cfi_ChunkFadeDatas[%s].fadeData;".formatted(drawId));

			if (animationType == JAGGED || animationType == DISPLACEMENT || fadeType == VERTEX)
				rand("rand", "%s + vec3(%s)".formatted(localPos, drawId));
		}

		if (isFadeEnabled) {
			newLine("cfi_FadeFactor = chunkFadeData.w;");

			if (fadeType == BLOCK)
				newLine("cfi_BlockSeed = %s + vec3(%s);".formatted(localPos, drawId));
			if (fadeType == VERTEX)
				newLine("cfi_RefFactor = cfi_FadeFactor > rand ? 1.0 : cfi_FadeFactor / rand;");
			if (fadeType == LINED)
				newLine("cfi_RefFactor = %s.y / 16.0;".formatted(localPos));
		}

		return this;
	}

	public FadeShader vertInitMod(String localPos, String position, boolean modifyLocal, String drawId, boolean addCurvature) {
		if (!isModEnabled)
			return this;

		if (isAnimationEnabled)
			calculateVertexDisplacement(localPos, position, modifyLocal, drawId);

		if (addCurvature && isCurvatureEnabled)
			newLine("%s.y -= dot(%s, %s) / %s;".formatted(modifyLocal ? localPos : position, position, position, worldCurvature));

		return this;
	}

	public FadeShader fragColorMod(String color, String fog) {
		if (!isModEnabled || !isFadeEnabled)
			return this;

		newLine("if (cfi_FadeFactor >= 0.0 && cfi_FadeFactor < 1.0) {");

		calculateFade("float fade = ");
		newLine("%s = mix(%s, %s, fade);"
			.formatted(color, fog, color));

		newLine("}");

		return this;
	}

	public FadeShader calculateVertexDisplacement(String localPos, String position, boolean modifyLocal, String drawId) {
		switch (animationType) {
			case FULL:
			case JAGGED:
				newLine("%s += chunkFadeData.xyz".formatted(modifyLocal ? localPos : position));
				if (animationType == JAGGED)
					append(" * rand");
				append(";");
				break;
			case DISPLACEMENT:
				newLine("if (%s.x != 0.0 && %s.y != 0.0 && %s.z != 0.0 && %s.x != 16.0 && %s.y != 16.0 && %s.z != 16.0) {"
					.replace("%s", localPos));
				randAppend("rand2", "%s - vec3(%s)".formatted(localPos, drawId));
				randAppend("rand3", "%s + vec3(%s * uint(2))".formatted(localPos, drawId));
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

	public void rand(String name, String vector) {
		newLine("float %s = fract(sin(dot(%s, vec3(12.9898, 78.233, 132.383))) * 43758.5453);"
			.formatted(name, vector));
		newLine("if (%s == 0.0) %s = 0.001;".formatted(name, name));
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
