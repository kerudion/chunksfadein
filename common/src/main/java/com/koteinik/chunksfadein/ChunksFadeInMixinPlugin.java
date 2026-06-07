package com.koteinik.chunksfadein;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ChunksFadeInMixinPlugin implements IMixinConfigPlugin {
	private boolean hasClass(String className) {
		return getClass().getClassLoader().getResource(className.replace('.', '/') + ".class") != null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		boolean isV6 = mixinClassName.contains("v6");
		boolean isV8 = mixinClassName.contains("v8");
		boolean isGenericSodium = mixinClassName.contains("sodium") && !isV6 && !isV8;
		boolean isEmbeddium = mixinClassName.contains("embeddium");
		boolean isMonocle = mixinClassName.contains("monocle");
		boolean isNoIris = mixinClassName.contains("no_iris");
		boolean isIris = mixinClassName.contains("iris");
		boolean isDH = mixinClassName.contains("dh");
		boolean isSable = mixinClassName.contains("sable");

		boolean hasV6 = hasClass("net.caffeinemc.mods.sodium.client.gui.options.OptionPage");
		boolean hasV8 = hasClass("net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint");
		boolean hasEmbeddium = hasClass("org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer");
		boolean hasIris = hasClass("net.irisshaders.iris.api.v0.IrisApi");
		boolean hasDH = hasClass("com.seibel.distanthorizons.api.DhApi");
		boolean hasSable = hasClass("dev.ryanhcode.sable.api.SubLevelHelper");

		boolean allow = true;

		if (isNoIris && !hasIris)
			allow = false;

		if (isIris && !hasIris)
			allow = false;

		if (isDH && !hasDH)
			allow = false;

		if (isSable && !hasSable)
			allow = false;

		if (isEmbeddium && !hasEmbeddium)
			allow = false;

		if (isGenericSodium && !(hasV6 || hasV8))
			allow = false;

		if (isV6 && !hasV6)
			allow = false;

		if (isV8 && !hasV8)
			allow = false;

		if (isMonocle && !(hasEmbeddium && hasIris))
			allow = false;

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
