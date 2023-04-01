package com.koteinik.chunksfadein;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

public class ChunkFadeInMixinConfig implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean isIrisShaderMixinV12 = mixinClassName
                .equals("com.koteinik.chunksfadein.mixin.iris.v12IrisChunkShaderInterfaceMixin");
        boolean isIrisShaderMixinV14 = mixinClassName
                .equals("com.koteinik.chunksfadein.mixin.iris.v14IrisChunkShaderInterfaceMixin");
        boolean isIrisShaderMixinV15 = mixinClassName
                .equals("com.koteinik.chunksfadein.mixin.iris.v15IrisChunkShaderInterfaceMixin");
        boolean isIrisRegionMixin = mixinClassName
                .equals("com.koteinik.chunksfadein.mixin.iris.IrisRegionChunkRendererMixin");

        boolean isIrisMixin = isIrisShaderMixinV12 || isIrisShaderMixinV14 || isIrisShaderMixinV15 || isIrisRegionMixin;

        if (!isIrisMixin)
            return true;

        try {
            Class.forName("net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt", false,
                    getClass().getClassLoader());
            if (isIrisRegionMixin)
                return true;

            boolean isIrisV15 = FabricLoader.getInstance().getModContainer("iris").get().getMetadata()
                    .getVersion().compareTo(Version.parse("1.5")) >= 0;
            boolean isIrisV12 = FabricLoader.getInstance().getModContainer("iris").get().getMetadata()
                    .getVersion().compareTo(Version.parse("1.4")) < 0;

            if (isIrisV12 && isIrisShaderMixinV12)
                return true;
            else if (!isIrisV12 && isIrisV15 && isIrisShaderMixinV15)
                return true;
            else if (!isIrisV12 && !isIrisV15 && isIrisShaderMixinV14)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
