package com.koteinik.chunksfadein;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class NetworkUtils {
	public static JsonElement executeGet(String rawUrl, Map<String, String> headers, Map<String, String> params) {
		try {
			StringBuilder paramsStr = new StringBuilder();

			int i = 0;
			for (Entry<String, String> entry : params.entrySet()) {
				if (i == 0)
					paramsStr.append("?");
				else
					paramsStr.append("&");

				paramsStr.append(encode(entry.getKey()));
				paramsStr.append("=");
				paramsStr.append(encode(entry.getValue()));

				i++;
			}

			URL url = URL.of(URI.create(rawUrl + paramsStr), null);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			headers.forEach(con::setRequestProperty);

			con.setConnectTimeout(10000);
			con.setReadTimeout(5000);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			StringBuilder body = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
				body.append(inputLine);

			in.close();

			con.disconnect();

			return JsonParser.parseString(body.toString());
		} catch (Exception e) {
			Logger.warn("Failed to execute GET http request on '" + rawUrl + "'!");
			e.printStackTrace();
			return null;
		}
	}

	private static String encode(String str) throws UnsupportedEncodingException {
		return URLEncoder.encode(str, "UTF-8");
	}
}
