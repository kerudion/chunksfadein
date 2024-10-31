package com.koteinik.chunksfadein;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ChunksFadeInMixinPlugin implements IMixinConfigPlugin {
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		boolean isIrisMixin = mixinClassName.startsWith("com.koteinik.chunksfadein.mixin.iris");

		if (!isIrisMixin)
			return true;

		try {
			Class.forName("net.irisshaders.iris.api.v0.IrisApi");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
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
