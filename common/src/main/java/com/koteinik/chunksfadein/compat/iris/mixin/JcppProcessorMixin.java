package com.koteinik.chunksfadein.compat.iris.mixin;

import com.koteinik.chunksfadein.compat.iris.IrisPatcher;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.AnimationType;
import com.koteinik.chunksfadein.core.FadeType;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;
import org.anarres.cpp.Preprocessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = JcppProcessor.class, remap = false)
public class JcppProcessorMixin {
	private static final Set<String> whitelistedPrograms = new HashSet<>() {
		{
			add("shadow");
			add("shadow_solid");
			add("shadow_cutout");
			add("shadow_water");
			add("gbuffers_terrain");
			add("gbuffers_terrain_solid");
			add("gbuffers_terrain_cutout");
			add("gbuffers_water");
			add("dh_terrain");
			add("dh_water");
			add("dh_shadow");
		}
	};

	@Inject(
		method = "glslPreprocessSource",
		at = @At(
			value = "INVOKE",
			target = "Lorg/anarres/cpp/Preprocessor;setListener(Lorg/anarres/cpp/PreprocessorListener;)V"
		)
	)
	private static void modifyGlslPreprocessSource(String source,
	                                               Iterable<StringPair> environmentDefines,
	                                               CallbackInfoReturnable<String> cir,
	                                               @Local(name = "pp") Preprocessor pp) {
		if (!Config.isModEnabled)
			return;

		if (!source.contains("CHUNKS_FADE_IN_FORCE_DEFINES")
			&& !whitelistedPrograms.contains(IrisPatcher.currentShaderName.get()))
			return;

		try {
			pp.addMacro("CHUNKS_FADE_IN_ENABLED");

			if (Config.isFadeEnabled) {
				for (FadeType type : FadeType.values())
					pp.addMacro("CFI_FADE_" + type.name().toUpperCase(), type.ordinal() + "");

				pp.addMacro("CFI_FADE", Config.fadeType.ordinal() + "");
			}

			if (Config.isAnimationEnabled) {
				for (AnimationType type : AnimationType.values())
					pp.addMacro("CFI_ANIMATION_" + type.name().toUpperCase(), type.ordinal() + "");

				pp.addMacro("CFI_ANIMATION", Config.animationType.ordinal() + "");
			}

			if (Config.isCurvatureEnabled)
				pp.addMacro("CFI_CURVATURE", Config.worldCurvature + "");
		} catch (Exception ignored) {
		}
	}

	@ModifyVariable(
		method = "glslPreprocessSource",
		at = @At(
			value = "INVOKE",
			target = "Lorg/anarres/cpp/Preprocessor;setListener(Lorg/anarres/cpp/PreprocessorListener;)V"
		),
		argsOnly = true
	)
	private static String modifyGlslPreprocessSource2(String source) {
		if (!Config.isModEnabled)
			return source;

		if (!whitelistedPrograms.contains(IrisPatcher.currentShaderName.get())) {
			source += "\nvoid _cfi_ignoreMarker() {}\n";
			return source;
		}

		if (source.contains("CHUNKS_FADE_IN_NO_MOD_INJECT")) source += "\nvoid _cfi_noInjectModMarker() {}\n";
		if (source.contains("CHUNKS_FADE_IN_NO_FRAG_MOD_INJECT")) source += "\nvoid _cfi_noInjectFragModMarker() {}\n";
		if (source.contains("CHUNKS_FADE_IN_NO_VERT_MOD_INJECT")) source += "\nvoid _cfi_noInjectVertModMarker() {}\n";
		if (source.contains("CHUNKS_FADE_IN_NO_INJECT")) source += "\nvoid _cfi_noInjectMarker() {}\n";
		if (source.contains("CHUNKS_FADE_IN_NO_CURVATURE")) source += "\nvoid _cfi_noCurvatureMarker() {}\n";
		if (source.contains("CHUNKS_FADE_IN_NO_LOD_MASK")) source += "\nvoid _cfi_noLodMaskMarker() {}\n";

		return source;
	}
}
