package com.koteinik.chunksfadein.crowdin;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code taken and modified from https://github.com/gbl/CrowdinTranslate
 */
public class TranslationsPack implements PackResources {
	@Override
	public IoSupplier<InputStream> getRootResource(String... names) {
		File file = new File(Translations.getPackRootDir(), names[0]);
		if (file.exists())
			return IoSupplier.create(file.toPath());

		return null;
	}

	@Override
	public IoSupplier<InputStream> getResource(PackType type, ResourceLocation loc) {
		return this.getRootResource(type.getDirectory() + "/" + loc.getNamespace() + "/" + loc.getPath());
	}

	@Override
	public void listResources(PackType type, String namespace, String prefix, ResourceOutput consumer) {
		String[] files = new File(Translations.getPackRootDir(), "assets/" + namespace + "/" + prefix).list();
		if (files == null || files.length == 0)
			return;

		List<ResourceLocation> results = Arrays.asList(files)
			.stream()
			.map(TranslationsPack::fromPath)
			.collect(Collectors.toList());

		for (ResourceLocation result : results)
			consumer.accept(result, getResource(type, result));
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return Set.of("chunksfadein");
	}

	@Override
	public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
		return null;
	}

	@Override
	public PackLocationInfo location() {
		return new PackLocationInfo(
			"chunksfadein-translations",
			Component.literal("Chunks Fade In internal pack with translations"),
			PackSource.DEFAULT,
			Optional.empty()
		);
	}

	@Override
	public void close() {}

	private static ResourceLocation fromPath(String path) {
		if (path.startsWith("assets/"))
			path = path.substring("assets/".length());
		String[] split = path.split("/", 2);

		return ResourceLocation.fromNamespaceAndPath(split[0], split[1]);
	}
}
