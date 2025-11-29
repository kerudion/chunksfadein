package com.koteinik.chunksfadein.crowdin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Translations {
	private static Map<String, String> enUs;
	private static volatile Map<String, Map<String, String>> translations = new ConcurrentHashMap<>();

	static {
		try (InputStream inputStream = Translations.class
			.getClassLoader()
			.getResourceAsStream("assets/chunksfadein/lang/en_us.json")) {
			BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(inputStream));
			enUs = parseJsonTranslations(new String(bis.readAllBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static void addTranslations(Map<String, Map<String, String>> newTranslations) {
		translations.putAll(newTranslations);
	}

	public static MutableComponent translatable(String key) {
		return MutableComponent.create(new ComponentContents() {
			@Override
			public @NotNull Type<?> type() {
				return PlainTextContents.TYPE;
			}

			@Override
			public <T> @NotNull Optional<T> visit(FormattedText.@NotNull ContentConsumer<T> consumer) {
				return consumer.accept(Translations.resolve(key));
			}

			@Override
			public <T> @NotNull Optional<T> visit(FormattedText.@NotNull StyledContentConsumer<T> consumer, @NotNull Style style) {
				return consumer.accept(style, Translations.resolve(key));
			}
		});
	}

	public static synchronized String resolve(String key) {
		String language = Minecraft.getInstance().options.languageCode;

		Map<String, String> map = translations.get(language);
		if (map != null) {
			String value = map.get(key);
			if (value != null) return value;
		}

		return getDefault(key);
	}

	public static String getDefault(String key) {
		String value = enUs.get(key);
		if (value == null) return "null";
		return value;
	}

	public static void download() {
		new TranslationsDownloader().start();
	}

	static Map<String, String> parseJsonTranslations(String json) {
		JsonObject jsonPairs = JsonParser.parseString(json).getAsJsonObject();

		return jsonPairs.asMap().entrySet().stream()
			.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getAsString()))
			.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
	}
}
