package com.koteinik.chunksfadein.config;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.config.ConfigEntry.Type;
import com.koteinik.chunksfadein.core.*;
import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.platform.Services;
import com.moandjiezana.toml.Toml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.koteinik.chunksfadein.gui.SettingsScreen.*;

public class Config {
	private static final int CONFIG_VERSION = 5;

	public static final double MIN_FADE_TIME = 0.01;
	public static final double MAX_FADE_TIME = 10;
	public static final double MIN_ANIMATION_TIME = 0.01;
	public static final double MAX_ANIMATION_TIME = 10;
	public static final double MIN_ANIMATION_OFFSET = -128;
	public static final double MAX_ANIMATION_OFFSET = 128;
	public static final double MIN_ANIMATION_FACTOR = 0.01;
	public static final double MAX_ANIMATION_FACTOR = 1;
	public static final double MIN_ANIMATION_ANGLE = 0;
	public static final double MAX_ANIMATION_ANGLE = 90;
	public static final int MIN_CURVATURE = -65536;
	public static final int MAX_CURVATURE = -MIN_CURVATURE;

	public static final String CONFIG_VERSION_KEY = "config-version";
	public static final String MOD_ENABLED_KEY = "mod-enabled";
	public static final String SHOW_MOD_TAB_IN_SETTINGS_KEY = "show-mod-tab-in-settings";
	public static final String UPDATE_NOTIFIER_ENABLED_KEY = "update-notifier-enabled";
	public static final String FADE_ENABLED_KEY = "fade-enabled";
	public static final String FADE_PATCH_SHADERS_KEY = "fade-patch-shaders";
	public static final String FADE_TIME_KEY = "fade-time";
	public static final String FADE_TYPE_KEY = "fade-type";
	public static final String FADE_CURVE_KEY = "fade-curve";
	public static final String FADE_MIX_TYPE_KEY = "fade-mix-type";
	public static final String FOG_OVERRIDE_KEY = "fog-override";
	public static final String FADE_NEAR_PLAYER_KEY = "fade-near-player";
	public static final String ANIMATION_ENABLED_KEY = "animation-enabled";
	public static final String ANIMATION_PATCH_SHADERS_KEY = "animation-patch-shaders";
	public static final String ANIMATION_TYPE_KEY = "animation-type";
	public static final String ANIMATE_NEAR_PLAYER_KEY = "animate-near-player";
	public static final String ANIMATE_WITH_DH_KEY = "fade-with-dh";
	public static final String ANIMATION_TIME_KEY = "animation-time";
	public static final String ANIMATION_CURVE_KEY = "animation-curve";
	public static final String ANIMATION_OFFSET_KEY = "animation-offset";
	public static final String ANIMATION_ANGLE_KEY = "animation-angle";
	public static final String ANIMATION_FACTOR_KEY = "animation-factor";
	public static final String CURVATURE_ENABLED_KEY = "world-curvature-enabled";
	public static final String CURVATURE_PATCH_SHADERS_KEY = "world-curvature-patch-shaders";
	public static final String CURVATURE_KEY = "world-curvature";

	private static final Map<String, ConfigEntry<?>> entries = new HashMap<>();
	private static File configFile;

	public static boolean isModEnabled;
	public static boolean isUpdateNotifierEnabled;
	public static boolean showModTabInSettings;
	public static boolean isFadeEnabled;
	public static boolean fadeNearPlayer;
	public static boolean patchShaderFade;
	public static boolean isAnimationEnabled;
	public static boolean animateNearPlayer;
	public static boolean animateWithDH;
	public static boolean patchShaderAnimation;
	public static boolean isCurvatureEnabled;
	public static boolean patchShaderCurvature;

	public static float animationAngle; // for FULL
	public static float animationOffset; // for FULL, JAGGED
	public static float animationFactor; // for DISPLACEMENT, SCALE
	public static float animationChangePerMs;
	public static float fadeChangePerMs;

	public static int worldCurvature;
	public static int configVersion;

	public static FadeType fadeType;
	public static FadeCurve fadeCurve;
	public static FadeMixType fadeMixType;
	public static FogOverrideMode fogOverrideMode;
	public static AnimationType animationType;
	public static AnimationCurve animationCurve;

	static {
		addEntry(new ConfigEntry<>(CONFIG_VERSION, CONFIG_VERSION_KEY, "!!!DO NOT CHANGE THIS!!!", Type.INTEGER))
			.addListener((o) -> configVersion = o);
		addEntry(new ConfigEntryEnum<>(
			AnimationCurve.class,
			AnimationCurve.EASE_OUT,
			tooltip(ANIMATION_CURVE),
			ANIMATION_CURVE_KEY
		))
			.addListener((o) -> animationCurve = o);
		addEntry(new ConfigEntryEnum<>(
			FadeType.class,
			FadeType.FULL,
			tooltip(FADE_TYPE),
			FADE_TYPE_KEY
		))
			.addListener((o) -> fadeType = o);
		addEntry(new ConfigEntryEnum<>(
			FadeCurve.class,
			FadeCurve.QUINTIC,
			tooltip(FADE_CURVE),
			FADE_CURVE_KEY
		))
			.addListener((o) -> fadeCurve = o);
		addEntry(new ConfigEntryEnum<>(
			FadeMixType.class,
			FadeMixType.LINEAR,
			tooltip(FADE_MIX_TYPE),
			FADE_MIX_TYPE_KEY
		))
			.addListener((o) -> fadeMixType = o);
		addEntry(new ConfigEntryEnum<>(
			FogOverrideMode.class,
			FogOverrideMode.CYLINDRICAL,
			tooltip(FOG_OVERRIDE),
			FOG_OVERRIDE_KEY
		))
			.addListener((o) -> fogOverrideMode = o);
		addEntry(new ConfigEntryEnum<>(
			AnimationType.class,
			AnimationType.FULL,
			tooltip(ANIMATION_TYPE),
			ANIMATION_TYPE_KEY
		))
			.addListener((o) -> animationType = o);
		addEntry(new ConfigEntry<>(
			16384,
			CURVATURE_KEY,
			tooltip(CURVATURE),
			Type.INTEGER
		))
			.addListener((o) -> worldCurvature = o);

		addEntry(new ConfigEntryDoubleLimitable(
			MIN_FADE_TIME,
			MAX_FADE_TIME,
			0.75,
			tooltip(FADE_TIME),
			FADE_TIME_KEY
		))
			.addListener((o) -> fadeChangePerMs = fadeChangeFromSeconds(o));
		addEntry(new ConfigEntryDoubleLimitable(
			MIN_ANIMATION_TIME,
			MAX_ANIMATION_TIME,
			2.56,
			tooltip(ANIMATION_TIME),
			ANIMATION_TIME_KEY
		))
			.addListener((o) -> animationChangePerMs = animationChangeFromSeconds(o));
		addEntry(new ConfigEntryDoubleLimitable(
			MIN_ANIMATION_OFFSET,
			MAX_ANIMATION_OFFSET,
			-64,
			tooltip(ANIMATION_OFFSET),
			ANIMATION_OFFSET_KEY
		))
			.addListener((o) -> animationOffset = o.floatValue());
		addEntry(new ConfigEntryDoubleLimitable(
			MIN_ANIMATION_ANGLE,
			MAX_ANIMATION_ANGLE,
			0,
			tooltip(ANIMATION_ANGLE),
			ANIMATION_ANGLE_KEY
		))
			.addListener((o) -> animationAngle = o.floatValue());
		addEntry(new ConfigEntryDoubleLimitable(
			MIN_ANIMATION_FACTOR,
			MAX_ANIMATION_FACTOR,
			1,
			tooltip(ANIMATION_FACTOR),
			ANIMATION_FACTOR_KEY
		))
			.addListener((o) -> animationFactor = o.floatValue());

		addEntry(new ConfigEntry<>(true, MOD_ENABLED_KEY, tooltip(MOD_ENABLED), Type.BOOLEAN))
			.addListener((o) -> isModEnabled = o);
		addEntry(new ConfigEntry<>(true, UPDATE_NOTIFIER_ENABLED_KEY, tooltip(UPDATE_NOTIFIER_ENABLED), Type.BOOLEAN))
			.addListener((o) -> isUpdateNotifierEnabled = o);
		addEntry(new ConfigEntry<>(true, SHOW_MOD_TAB_IN_SETTINGS_KEY, tooltip(MOD_TAB_ENABLED), Type.BOOLEAN))
			.addListener((o) -> showModTabInSettings = o);
		addEntry(new ConfigEntry<>(true, FADE_ENABLED_KEY, tooltip(FADE_ENABLED), Type.BOOLEAN))
			.addListener((o) -> isFadeEnabled = o);
		addEntry(new ConfigEntry<>(true, FADE_PATCH_SHADERS_KEY, tooltip(FADE_PATCH_SHADERS), Type.BOOLEAN))
			.addListener((o) -> patchShaderFade = o);
		addEntry(new ConfigEntry<>(true, FADE_NEAR_PLAYER_KEY, tooltip(FADE_NEAR_PLAYER), Type.BOOLEAN))
			.addListener((o) -> fadeNearPlayer = o);
		addEntry(new ConfigEntry<>(false, ANIMATION_ENABLED_KEY, tooltip(ANIMATION_ENABLED), Type.BOOLEAN))
			.addListener((o) -> isAnimationEnabled = o);
		addEntry(new ConfigEntry<>(true, ANIMATION_PATCH_SHADERS_KEY, tooltip(ANIMATION_PATCH_SHADERS), Type.BOOLEAN))
			.addListener((o) -> patchShaderAnimation = o);
		addEntry(new ConfigEntry<>(true, ANIMATE_NEAR_PLAYER_KEY, tooltip(ANIMATE_NEAR_PLAYER), Type.BOOLEAN))
			.addListener((o) -> animateNearPlayer = o);
		addEntry(new ConfigEntry<>(false, ANIMATE_WITH_DH_KEY, tooltip(ANIMATE_WITH_DH), Type.BOOLEAN))
			.addListener((o) -> animateWithDH = o);
		addEntry(new ConfigEntry<>(false, CURVATURE_ENABLED_KEY, tooltip(CURVATURE_ENABLED), Type.BOOLEAN))
			.addListener((o) -> isCurvatureEnabled = o);
		addEntry(new ConfigEntry<>(true, CURVATURE_PATCH_SHADERS_KEY, tooltip(CURVATURE_PATCH_SHADERS), Type.BOOLEAN))
			.addListener((o) -> patchShaderCurvature = o);
	}

	private static String tooltip(String key) {
		return tooltip(key, "tooltip");
	}

	private static String tooltip(String key, String custom) {
		return Translations.getDefault(key + "." + custom);
	}

	public static float fadeChangeFromSeconds(double seconds) {
		final float secondsInMs = (float) (seconds * 1E+3);

		return 1f / secondsInMs;
	}

	public static float secondsFromFadeChange() {
		return 1f / fadeChangePerMs / 1E+3f;
	}

	public static float animationChangeFromSeconds(double seconds) {
		final float secondsInMs = (float) (seconds * 1E+3);

		return 1 / secondsInMs;
	}

	public static double secondsFromAnimationChange() {
		return 1 / animationChangePerMs / 1E+3;
	}

	public static void load() {
		configFile = new File(Services.PLATFORM.getConfigDirectory(), "chunksfadein.properties");
		Toml toml = new Toml();

		try {
			if (!configFile.exists())
				configFile.createNewFile();

			toml.read(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (ConfigEntry<?> entry : entries.values())
			entry.load(toml);

		if (configVersion < 3)
			setDouble(ANIMATION_OFFSET_KEY, -getDouble(ANIMATION_OFFSET_KEY));

		if (configVersion < 5)
			if (getEnum(FADE_TYPE_KEY) != FadeType.FULL)
				setEnum(FADE_CURVE_KEY, FadeCurve.LINEAR);

		setInteger(CONFIG_VERSION_KEY, CONFIG_VERSION);
		save();
	}

	public static void save() {
		List<String> sortedEntries = entries.values()
			.stream()
			.sorted((a, b) -> {
				if (a.configKey.equals(CONFIG_VERSION_KEY))
					return -1;
				if (b.configKey.equals(CONFIG_VERSION_KEY))
					return 1;

				return a.configKey.compareTo(b.configKey);
			})
			.map(ConfigEntry::toString)
			.toList();

		String string = String.join("\n", sortedEntries);

		try {
			if (!configFile.exists())
				configFile.createNewFile();

			BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
			writer.write(string);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("Failed to save config! If this is an error, please create an issue on github!");
		}
	}

	public static boolean setBoolean(String key, Boolean value) {
		ConfigEntry<Boolean> entry = get(key);

		return entry.set(value);
	}

	public static int setInteger(String key, Integer value) {
		ConfigEntry<Integer> entry = get(key);

		return entry.set(value);
	}

	public static <T extends Enum<T> & TranslatableEnum> T setEnum(String key, T value) {
		ConfigEntry<T> entry = get(key);

		return entry.set(value);
	}

	public static double setDouble(String key, Double value) {
		ConfigEntry<Double> entry = get(key);

		return entry.set(value);
	}

	public static boolean getBoolean(String key) {
		ConfigEntry<Boolean> entry = get(key);

		return entry.get();
	}

	public static int getInteger(String key) {
		ConfigEntry<Integer> entry = get(key);

		return entry.get();
	}

	public static <T extends Enum<T> & TranslatableEnum> T getEnum(String key) {
		ConfigEntry<T> entry = get(key);

		return entry.get();
	}

	public static double getDouble(String key) {
		ConfigEntry<Double> entry = get(key);

		return entry.get();
	}

	public static boolean flipBoolean(String key) {
		return setBoolean(key, !getBoolean(key));
	}

	public static double getMin(String key) {
		ConfigEntry<Double> entry = get(key);

		if (entry instanceof ConfigEntryDoubleLimitable limitable)
			return limitable.getMin();

		throw new UnsupportedOperationException();
	}

	public static double getMax(String key) {
		ConfigEntry<Double> entry = get(key);

		if (entry instanceof ConfigEntryDoubleLimitable limitable)
			return limitable.getMax();

		throw new UnsupportedOperationException();
	}

	public static void reset(String key) {
		get(key).reset();
	}

	@SuppressWarnings("unchecked")
	private static <T> ConfigEntry<T> get(String key) {
		return (ConfigEntry<T>) entries.get(key);
	}

	private static <T> ConfigEntry<T> addEntry(ConfigEntry<T> entry) {
		entries.put(entry.configKey, entry);
		return entry;
	}
}
