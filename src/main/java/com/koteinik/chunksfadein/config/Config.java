package com.koteinik.chunksfadein.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.ConfigEntry.Type;
import com.koteinik.chunksfadein.core.Curves;
import com.koteinik.chunksfadein.core.FadeTypes;
import com.moandjiezana.toml.Toml;

import net.fabricmc.loader.api.FabricLoader;

public class Config {
    public static final Long CONFIG_VERSION = 2L;

    public static final double MAX_FADE_TIME = 10;
    public static final double MAX_ANIMATION_TIME = 10;
    public static final double MAX_ANIMATION_OFFSET = 319;
    public static final int MIN_CURVATURE = -65536;
    public static final int MAX_CURVATURE = -MIN_CURVATURE;

    public static final String CONFIG_VERSION_KEY = "config-version";
    public static final String MOD_ENABLED_KEY = "mod-enabled";
    public static final String SHOW_MOD_BUTTON_IN_SETTINGS_KEY = "show-mod-button-in-settings";
    public static final String UPDATE_NOTIFIER_ENABLED_KEY = "update-notifier-enabled";
    public static final String FADE_ENABLED_KEY = "fade-enabled";
    public static final String FADE_TIME_KEY = "fade-time";
    public static final String FADE_TYPE_KEY = "fade-type";
    public static final String FADE_NEAR_PLAYER_KEY = "fade-near-player";
    public static final String ANIMATION_ENABLED_KEY = "animation-enabled";
    public static final String ANIMATE_NEAR_PLAYER_KEY = "animate-near-player";
    public static final String ANIMATION_TIME_KEY = "animation-time";
    public static final String ANIMATION_CURVE_KEY = "animation-curve";
    public static final String ANIMATION_OFFSET_KEY = "animation-offset";
    public static final String CURVATURE_ENABLED_KEY = "world-curvature-enabled";
    public static final String CURVATURE_KEY = "world-curvature";

    private static final Map<String, ConfigEntry<?>> entries = new HashMap<>();
    private static File configFile;

    public static boolean isModEnabled;
    public static boolean isFadeEnabled;
    public static boolean isAnimationEnabled;
    public static boolean isCurvatureEnabled;
    public static boolean isUpdateNotifierEnabled;
    public static boolean showModButtonInSettings;
    public static boolean animateNearPlayer;
    public static boolean fadeNearPlayer;

    public static float animationInitialOffset;
    public static float animationChangePerNano;
    public static float fadeChangePerNano;

    public static int worldCurvature;

    public static FadeTypes fadeType;
    public static Curves animationCurve;

    static {
        addEntry(new ConfigEntry<Integer>(Curves.EASE_OUT.ordinal(), ANIMATION_CURVE_KEY, Type.INTEGER))
            .addListener((o) -> animationCurve = Curves.values()[MathUtils.clamp(o, 0,
                Curves.values().length - 1)]);
        addEntry(new ConfigEntry<Integer>(FadeTypes.FULL.ordinal(), FADE_TYPE_KEY, Type.INTEGER))
            .addListener((o) -> fadeType = FadeTypes.values()[MathUtils.clamp(o, 0,
                Curves.values().length - 1)]);
        addEntry(new ConfigEntry<Integer>(16384, CURVATURE_KEY, Type.INTEGER))
            .addListener((o) -> worldCurvature = o);

        addEntry(new ConfigEntryDoubleLimitable(0.01, MAX_FADE_TIME, 1, FADE_TIME_KEY))
            .addListener((o) -> fadeChangePerNano = fadeChangeFromSeconds(o));
        addEntry(new ConfigEntryDoubleLimitable(0.01, MAX_ANIMATION_TIME, 2.56, ANIMATION_TIME_KEY))
            .addListener((o) -> animationChangePerNano = animationChangeFromSeconds(o));
        addEntry(new ConfigEntryDoubleLimitable(1, MAX_ANIMATION_OFFSET, 64, ANIMATION_OFFSET_KEY))
            .addListener((o) -> animationInitialOffset = (o).floatValue());

        addEntry(new ConfigEntry<Boolean>(true, MOD_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isModEnabled = o);
        addEntry(new ConfigEntry<Boolean>(true, FADE_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isFadeEnabled = o);
        addEntry(new ConfigEntry<Boolean>(false, ANIMATION_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isAnimationEnabled = o);
        addEntry(new ConfigEntry<Boolean>(true, CURVATURE_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isCurvatureEnabled = o);
        addEntry(new ConfigEntry<Boolean>(true, UPDATE_NOTIFIER_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isUpdateNotifierEnabled = o);
        addEntry(new ConfigEntry<Boolean>(true, SHOW_MOD_BUTTON_IN_SETTINGS_KEY, Type.BOOLEAN))
            .addListener((o) -> showModButtonInSettings = o);
        addEntry(new ConfigEntry<Boolean>(true, ANIMATE_NEAR_PLAYER_KEY, Type.BOOLEAN))
            .addListener((o) -> animateNearPlayer = o);
        addEntry(new ConfigEntry<Boolean>(true, FADE_NEAR_PLAYER_KEY, Type.BOOLEAN))
            .addListener((o) -> fadeNearPlayer = o);
    }

    public static float fadeChangeFromSeconds(double seconds) {
        final float secondsInMs = (float) (seconds * 1E+9);

        return 1f / secondsInMs;
    }

    public static float secondsFromFadeChange() {
        return 1f / fadeChangePerNano / 1E+9f;
    }

    public static float animationChangeFromSeconds(double seconds) {
        final float secondsInMs = (float) (seconds * 1E+9);

        return 1 / secondsInMs;
    }

    public static double secondsFromAnimationChange() {
        return (double) (1 / animationChangePerNano / 1E+9);
    }

    public static void load() {
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "chunksfadein.properties");
        Toml toml = new Toml();

        try {
            if (!configFile.exists())
                configFile.createNewFile();

            toml.read(configFile);
        } catch (Exception e) {
        }

        for (ConfigEntry<?> entry : entries.values())
            entry.load(toml);
    }

    public static void save() {
        String string = "";

        string += CONFIG_VERSION_KEY + " = " + CONFIG_VERSION + "\n";
        for (ConfigEntry<?> entry : entries.values())
            string += entry.toString();

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

    public static void setInteger(String key, Integer value) {
        ConfigEntry<Integer> entry = get(key);

        entry.set(value);
    }

    public static void setBoolean(String key, Boolean value) {
        ConfigEntry<Boolean> entry = get(key);

        entry.set(value);
    }

    public static void setDouble(String key, Double value) {
        ConfigEntry<Double> entry = get(key);

        entry.set(value);
    }

    public static boolean getBoolean(String key) {
        ConfigEntry<Boolean> entry = get(key);

        return entry.get();
    }

    public static int getInteger(String key) {
        ConfigEntry<Integer> entry = get(key);

        return entry.get();
    }

    public static double getDouble(String key) {
        ConfigEntry<Double> entry = get(key);

        return entry.get();
    }

    public static void flipBoolean(String key) {
        setBoolean(key, !getBoolean(key));
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
