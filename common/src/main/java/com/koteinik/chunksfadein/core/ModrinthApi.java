package com.koteinik.chunksfadein.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.NetworkUtils;
import com.koteinik.chunksfadein.platform.Services;

import java.util.Map;

public class ModrinthApi {
	private static final String API_LINK = "https://api.modrinth.com/v2/";
	private static final String MOD_VERSIONS = "https://modrinth.com/mod/chunks-fade-in/version/";
	private static final String MOD_CDN = "https://cdn.modrinth.com/data/%s/versions/%s/%s";
	private static final String VERSIONS_ENDPOINT = API_LINK + "project/chunks-fade-in/version";
	private static SemanticVersion minecraftVersion;

	public static void load() {
		minecraftVersion = Services.PLATFORM.getMinecraftVersion();
	}

	public static ModrinthVersion getLatestModVersion() {
		try {
			JsonElement body = NetworkUtils.executeGet(
				VERSIONS_ENDPOINT,
				Map.of(),
				Map.of(
					"featured", "true",
					"game_versions", "[\"" + minecraftVersion + "\"]",
					"loaders", "[\"" + (Services.PLATFORM.isForge() ? "neoforge" : "fabric") + "\"]"
				)
			);

			JsonObject first = body.getAsJsonArray().get(0).getAsJsonObject();

			String changelog = first.get("changelog").getAsString();
			String cleanVersion = first.get("version_number").getAsString().replace("v", "")
			                           .replace("-fabric", "")
			                           .replace("-neoforge", "");

			String projectId = first.get("project_id").getAsString();
			String versionId = first.get("id").getAsString();
			String file = first.get("files").getAsJsonArray().asList().stream()
			                   .filter(e -> e.getAsJsonObject().get("primary").getAsBoolean())
			                   .findAny()
			                   .get()
			                   .getAsJsonObject()
			                   .get("filename")
			                   .getAsString();

			String downloadUrl = MOD_CDN.formatted(projectId, versionId, file);

			return new ModrinthVersion(new SemanticVersion(cleanVersion, false), changelog, downloadUrl);
		} catch (Exception e) {
			Logger.warn("Failed to get latest mod version!");
			e.printStackTrace();
			return null;
		}
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
