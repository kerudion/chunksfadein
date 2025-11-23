package com.koteinik.chunksfadein.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koteinik.chunksfadein.NetworkUtils;
import com.koteinik.chunksfadein.platform.Services;

import java.util.List;
import java.util.Map;

public class ModrinthApi {
	private static final String API_LINK = "https://api.modrinth.com/v2/";
	private static final String MOD_CDN = "https://cdn.modrinth.com/data/%s/versions/%s/%s";
	private static final String VERSIONS_ENDPOINT = API_LINK + "project/chunks-fade-in/version";
	private static SemanticVersion minecraftVersion;

	public static void load() {
		minecraftVersion = Services.PLATFORM.getMinecraftVersion();
	}

	public static List<ModrinthVersion> getLatestVersions() {
		JsonArray body = NetworkUtils.executeGet(
			VERSIONS_ENDPOINT,
			Map.of(),
			Map.of(
				"featured", "true",
				"game_versions", "[\"" + minecraftVersion + "\"]",
				"loaders", "[\"" + (Services.PLATFORM.isForge() ? "neoforge" : "fabric") + "\"]"
			)
		).getAsJsonArray();
		if (body.isEmpty())
			return List.of();

		return body.asList().stream().map(element -> {
			JsonObject version = element.getAsJsonObject();

			String changelog = version.get("changelog").getAsString();
			String cleanVersion = version.get("version_number").getAsString().replace("v", "")
				.replace("-fabric", "")
				.replace("-neoforge", "");

			String projectId = version.get("project_id").getAsString();
			String versionId = version.get("id").getAsString();
			String file = version.get("files").getAsJsonArray().asList().stream()
				.filter(e -> e.getAsJsonObject().get("primary").getAsBoolean())
				.findAny()
				.get()
				.getAsJsonObject()
				.get("filename")
				.getAsString();

			String downloadUrl = MOD_CDN.formatted(projectId, versionId, file);

			try {
				return new ModrinthVersion(new SemanticVersion(cleanVersion, false), changelog, downloadUrl);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).toList();
	}

	public static class ModrinthVersion {
		public final SemanticVersion version;
		public final String changelog;
		public final String downloadUrl;

		public ModrinthVersion(SemanticVersion version, String changelog, String downloadUrl) {
			this.version = version;
			this.changelog = changelog;
			this.downloadUrl = downloadUrl;
		}
	}
}
