package com.koteinik.chunksfadein.core;

import static com.koteinik.chunksfadein.config.Config.*;
import static com.koteinik.chunksfadein.core.FadeType.*;
import static com.koteinik.chunksfadein.core.AnimationType.*;

import java.util.ArrayList;
import java.util.List;

public class FadeShader {
	private List<String> lines = new ArrayList<>();

	public FadeShader vertOutUniforms() {
		newLine("struct ChunkFadeData { vec4 fadeData; };");
		newLine("layout(std140) uniform ubo_ChunkFadeDatas { ChunkFadeData Chunk_FadeDatas[256]; };");

		if (!isModEnabled || !isFadeEnabled)
			return this;

		newLine("flat out float cfi_FadeCoeff;");

		if (fadeType == BLOCK)
			newLine("out vec3 cfi_Pos;");
		if (fadeType == LINED)
			newLine("out float cfi_LocalHeight;");

		return this;
	}

	public FadeShader fragInVars() {
		if (!isModEnabled || !isFadeEnabled)
			return this;

		newLine("flat in float cfi_FadeCoeff;");

		if (fadeType == BLOCK)
			newLine("in vec3 cfi_Pos;");
		if (fadeType == LINED)
			newLine("in float cfi_LocalHeight;");

		return this;
	}

	public FadeShader vertMod(String localPos, String position, boolean modifyLocal, String drawId) {
		if (!isModEnabled)
			return this;

		if (isAnimationEnabled || isFadeEnabled) {
			newLine("vec4 chunkFadeData = Chunk_FadeDatas[%s].fadeData;".formatted(drawId));

			if (animationType == JAGGED || animationType == DISPLACEMENT || fadeType == VERTEX)
				rand("rand", "%s + vec3(%s)".formatted(localPos, drawId));
		}

		if (isFadeEnabled) {
			if (fadeType == BLOCK)
				newLine("cfi_Pos = %s + vec3(%s);".formatted(localPos, drawId));

			newLine("cfi_FadeCoeff = chunkFadeData.w");
			if (fadeType == VERTEX)
				append(" > rand ? 1.0 : chunkFadeData.w / rand");
			append(";");

			if (fadeType == LINED)
				newLine("cfi_LocalHeight = %s.y;".formatted(localPos));
		}

		if (isAnimationEnabled) {
			switch (animationType) {
				case DISPLACEMENT:
					newLine("if (%s.x != 0.0 && %s.y != 0.0 && %s.z != 0.0 && %s.x != 16.0 && %s.y != 16.0 && %s.z != 16.0) {"
						.replace("%s", localPos));
					randAppend("rand2", "%s - vec3(%s)".formatted(localPos, drawId));
					randAppend("rand3", "%s + vec3(%s * uint(2))".formatted(localPos, drawId));
					append("%s.xyz += vec3(rand - 0.5, rand2 - 0.5, rand3 - 0.5) * vec3(chunkFadeData.y);".formatted(modifyLocal ? localPos : position));
					append("}");
				case FULL:
				case JAGGED:
					newLine("%s += chunkFadeData.xyz".formatted(modifyLocal ? localPos : position));
					if (animationType == JAGGED)
						append(" * rand");
					append(";");
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
		}

		if (isCurvatureEnabled)
			newLine("%s.y -= dot(%s, %s) / %s;".formatted(modifyLocal ? localPos : position, position, position, worldCurvature));

		return this;
	}

	public FadeShader fragColorMod(String color, String fog) {
		if (!isModEnabled || !isFadeEnabled)
			return this;

		newLine("if (cfi_FadeCoeff >= 0.0 && cfi_FadeCoeff < 1.0) {");

		if (fadeType == LINED)
			newLine("float fade = cfi_LocalHeight <= cfi_FadeCoeff * 16.0 ? 1.0 : 0.0;");
		if (fadeType == BLOCK) {
			rand("rand", "floor(cfi_Pos)");
			newLine("float fade = cfi_FadeCoeff > rand ? 1.0 : cfi_FadeCoeff / rand;");
		}

		newLine("%s = mix(%s, %s, %s);"
			.formatted(color, fog, color, fadeType == FadeType.FULL || fadeType == VERTEX ? "cfi_FadeCoeff" : "fade"));

		newLine("}");

		return this;
	}

	public String dumpMultiline() {
		return String.join("\n", dumpList());
	}

	public String dumpSingleLine() {
		return String.join(" ", dumpList());
	}

	public String[] dumpArray() {
		return dumpList().toArray(String[]::new);
	}

	public List<String> dumpList() {
		List<String> lines = this.lines;
		this.lines = new ArrayList<>();

		return lines;
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

	private void rand(String name, String vector) {
		newLine("float %s = fract(sin(dot(%s, vec3(12.9898, 78.233, 132.383))) * 43758.5453);"
			.formatted(name, vector));
		newLine("if (%s == 0.0) %s = 0.001;".formatted(name, name));
	}

	private void randAppend(String name, String vector) {
		append("float %s = fract(sin(dot(%s, vec3(12.9898, 78.233, 132.383))) * 43758.5453);"
			.formatted(name, vector));
		append("if (%s == 0.0) %s = 0.001;".formatted(name, name));
	}
}
