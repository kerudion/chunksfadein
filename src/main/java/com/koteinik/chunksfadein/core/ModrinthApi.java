package com.koteinik.chunksfadein.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.koteinik.chunksfadein.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

public class ModrinthApi {
    private static final String API_LINK = "https://api.modrinth.com/v2/";
    private static final String MOD_VERSIONS = "https://modrinth.com/mod/chunks-fade-in/version/";
    private static final String VERSIONS_ENDPOINT = API_LINK + "project/chunks-fade-in/version";
    private static String minecraftVersion;

    public static void load() {
        minecraftVersion = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata()
                .getVersion().getFriendlyString();
    }

    @SuppressWarnings("unchecked")
    public static ModrinthVersion getLatestModVersion() {
        try {
            String rawBody = executeGet(VERSIONS_ENDPOINT,
                "featured=true",
                "game_versions=[\"" + minecraftVersion + "\"]");

            List<?> jsonList = new Gson().fromJson(rawBody, List.class);

            Object jsonObject = jsonList.get(0);
            Map<String, Object> jsonMap = (Map<String, Object>) jsonObject;

            Object versionObj = jsonMap.get("version_number");
            Object changelogObj = jsonMap.get("changelog");

            String version = (String) versionObj;
            String changelog = (String) changelogObj;

            return new ModrinthVersion(Version.parse(version.replaceAll("v", "")), changelog, MOD_VERSIONS + version);
        } catch (Exception e) {
            Logger.warn("Failed to get latest mod version! Cause: " + e.getMessage());
            return null;
        }
    }

    private static String executeGet(String urlString, String... paramsArr) {
        try {
            String paramsStr = "";
            for (int i = 0; i < paramsArr.length; i++) {
                if (i == 0)
                    paramsStr += "?";
                else
                    paramsStr += "&";

                paramsStr += URLEncoder.encode(paramsArr[i], "UTF-8").replaceAll("%3D", "=");
            }

            URL url = new URL(urlString + paramsStr);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");

            con.setConnectTimeout(10000);
            con.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            String body = "";

            while ((inputLine = in.readLine()) != null)
                body += inputLine;

            in.close();

            con.disconnect();
            return body;
        } catch (Exception e) {
            Logger.warn("Failed to execute get http request on '" + urlString + "'! Cause: " + e.getMessage());
            return null;
        }
    }

    public static class ModrinthVersion {
        public final Version version;
        public final String changelog;
        public final String downloadUrl;

        public ModrinthVersion(Version version, String changelog, String downloadUrl) {
            this.version = version;
            this.changelog = changelog;
            this.downloadUrl = downloadUrl;
        }
    }
}
