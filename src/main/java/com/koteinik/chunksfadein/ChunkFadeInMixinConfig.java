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
        boolean isRegionRenderer = mixinClassName
                .equals("com.koteinik.chunksfadein.mixin.iris.IrisRegionChunkRendererMixin");
        boolean isShaderInterface = mixinClassName
                .equals("com.koteinik.chunksfadein.mixin.iris.IrisChunkShaderInterfaceMixin");
        boolean isOldShaderInterface = mixinClassName
                .equals("com.koteinik.chunksfadein.mixin.iris.OldIrisChunkShaderInterfaceMixin");

        if (!(isRegionRenderer || isShaderInterface || isOldShaderInterface))
            return true;

        try {
            Class.forName("net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt", false,
                    getClass().getClassLoader());

            boolean isOldIris = FabricLoader.getInstance().getModContainer("iris").get()
                    .getMetadata()
                    .getVersion().compareTo(Version.parse("1.4")) < 0;

            if (isRegionRenderer) {
                Logger.info("allowing mixin " + mixinClassName);
                return true;
            } else if (isOldIris && isOldShaderInterface) {
                Logger.info("allowing old mixin " + mixinClassName);
                return true;
            } else if (!isOldIris && isShaderInterface) {
                Logger.info("allowing new mixin " + mixinClassName);
                return true;
            } else {
                Logger.info("disallowing mixin " + mixinClassName);
                return false;
            }
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