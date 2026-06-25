package com.koteinik.chunksfadein;

import DistantHorizons.libraries.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.koteinik.chunksfadein.platform.Services;
import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingEngine;
import com.seibel.distanthorizons.common.wrappers.VersionConstants;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ChunksFadeInMixinPlugin implements IMixinConfigPlugin {
	private static boolean dhIsBlaze = false;

	static {
		if (hasClass("com.seibel.distanthorizons.api.DhApi")) {
			EDhApiRenderingEngine api = EDhApiRenderingEngine.AUTO;

			File dhConfig = new File(Services.PLATFORM.getConfigDirectory(), "DistantHorizons.toml");
			if (dhConfig.exists()) {
				try (CommentedFileConfig cfg = CommentedFileConfig.builder(dhConfig).build()) {
					cfg.load();
					EDhApiRenderingEngine v = cfg.getEnum(
						"client.advanced.graphics.experimental.renderingEngine",
						EDhApiRenderingEngine.class
					);

					if (v != null) api = v;
				}
			}

			if (api == EDhApiRenderingEngine.AUTO)
				api = VersionConstants.INSTANCE.getDefaultRenderingEngine();

			dhIsBlaze = api == EDhApiRenderingEngine.BLAZE_3D;
		}
	}

	private static boolean hasClass(String className) {
		return ChunksFadeInMixinPlugin.class.getClassLoader().getResource(className.replace('.', '/') + ".class")
			!= null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		boolean isNoIrisMixin = mixinClassName.contains("no_iris");
		boolean isIrisMixin = !isNoIrisMixin && mixinClassName.contains("iris");
		boolean isDHMixin = mixinClassName.contains("dh");

		boolean hasIris = hasClass("net.irisshaders.iris.api.v0.IrisApi");
		boolean hasDH = hasClass("com.seibel.distanthorizons.api.DhApi");

		boolean isOGLMixin = mixinClassName.contains("ogl");
		boolean isBlazeMixin = mixinClassName.contains("blaze");

		boolean allow = true;

		if (isNoIrisMixin && hasIris)
			allow = false;

		if (isIrisMixin && !hasIris)
			allow = false;

		if (isDHMixin) {
			if (!hasDH)
				allow = false;

			if (isOGLMixin && dhIsBlaze)
				allow = false;

			if (isBlazeMixin && !dhIsBlaze)
				allow = false;
		}

		return allow;
	}

	@Override
	public void acceptTargets(Set<String> arg0, Set<String> arg1) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void onLoad(String arg0) {}

	@Override
	public void postApply(String arg0, ClassNode arg1, String arg2, IMixinInfo arg3) {}

	@Override
	public void preApply(String arg0, ClassNode arg1, String arg2, IMixinInfo arg3) {}
}
