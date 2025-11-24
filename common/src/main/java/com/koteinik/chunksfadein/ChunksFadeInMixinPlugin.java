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
		boolean isIrisMixin = mixinClassName.contains("iris");
		boolean isDHMixin = mixinClassName.contains("dh");

		boolean hasIris = hasClass("net.irisshaders.iris.api.v0.IrisApi");
		boolean hasDH = hasClass("com.seibel.distanthorizons.api.DhApi");

		if (isIrisMixin)
			return hasIris;

		if (isDHMixin)
			return hasDH;

		boolean hasEmbeddium = hasClass("org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer");
		if (mixinClassName.contains("embeddium"))
			return hasEmbeddium;

		if (mixinClassName.contains("sodium"))
			return !hasEmbeddium;

		boolean hasMonocle = hasEmbeddium && hasIris;
		if (mixinClassName.contains("monocle"))
			return hasMonocle;

		return true;
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
