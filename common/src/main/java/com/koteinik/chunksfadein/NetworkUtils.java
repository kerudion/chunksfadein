package com.koteinik.chunksfadein;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class NetworkUtils {
    public static JsonElement executeGet(String rawUrl, Map<String, String> headers, Map<String, String> params) {
        try {
            String paramsStr = "";

            int i = 0;
            for (Entry<String, String> entry : params.entrySet()) {
                if (i == 0)
                    paramsStr += "?";
                else
                    paramsStr += "&";

                paramsStr += encode(entry.getKey());
                paramsStr += "=";
                paramsStr += encode(entry.getValue());

                i++;
            }

            URL url = URL.of(URI.create(rawUrl + paramsStr), null);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.setRequestProperty("Content-Type", "application/json");
            headers.forEach((key, value) -> con.setRequestProperty(key, value));

            con.setConnectTimeout(10000);
            con.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            String body = "";

            while ((inputLine = in.readLine()) != null)
                body += inputLine;

            in.close();

            con.disconnect();

            return JsonParser.parseString(body);
        } catch (Exception e) {
            Logger.warn("Failed to execute GET http request on '" + rawUrl + "'! Cause: " + e.getMessage());
            return null;
        }
    }

    private static String encode(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}
