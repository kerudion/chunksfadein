package com.koteinik.chunksfadein.mixin.misc;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.koteinik.chunksfadein.crowdin.TranslationsPack;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
	@Shadow
	@Final
	private PackType type;

	@ModifyArg(
		method = "createReload",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;<init>(Lnet/minecraft/server/packs/PackType;Ljava/util/List;)V"),
		index = 1)
	private List<PackResources> onPostReload(List<PackResources> packs) {
		if (this.type != PackType.CLIENT_RESOURCES)
			return packs;

		List<PackResources> list = new ArrayList<>(packs);
		list.add(new TranslationsPack());

		return list;
	}
}
