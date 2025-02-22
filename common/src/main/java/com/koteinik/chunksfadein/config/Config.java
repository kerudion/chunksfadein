package com.koteinik.chunksfadein.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.ConfigEntry.Type;
import com.koteinik.chunksfadein.core.AnimationType;
import com.koteinik.chunksfadein.core.Curve;
import com.koteinik.chunksfadein.core.FadeType;
import com.koteinik.chunksfadein.platform.Services;
import com.moandjiezana.toml.Toml;

public class Config {
    private static final int CONFIG_VERSION = 3;

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
    public static final String FADE_TIME_KEY = "fade-time";
    public static final String FADE_TYPE_KEY = "fade-type";
    public static final String FADE_NEAR_PLAYER_KEY = "fade-near-player";
    public static final String ANIMATION_ENABLED_KEY = "animation-enabled";
    public static final String ANIMATION_TYPE_KEY = "animation-type";
    public static final String ANIMATE_NEAR_PLAYER_KEY = "animate-near-player";
    public static final String ANIMATION_TIME_KEY = "animation-time";
    public static final String ANIMATION_CURVE_KEY = "animation-curve";
    public static final String ANIMATION_OFFSET_KEY = "animation-offset";
    public static final String ANIMATION_ANGLE_KEY = "animation-angle";
    public static final String ANIMATION_FACTOR_KEY = "animation-factor";
    public static final String CURVATURE_ENABLED_KEY = "world-curvature-enabled";
    public static final String CURVATURE_KEY = "world-curvature";

    private static final Map<String, ConfigEntry<?>> entries = new HashMap<>();
    private static File configFile;

    public static boolean isModEnabled;
    public static boolean isFadeEnabled;
    public static boolean isAnimationEnabled;
    public static boolean isCurvatureEnabled;
    public static boolean isUpdateNotifierEnabled;
    public static boolean showModTabInSettings;
    public static boolean animateNearPlayer;
    public static boolean fadeNearPlayer;

    public static float animationAngle; // for FULL
    public static float animationOffset; // for FULL, JAGGED
    public static float animationFactor; // for DISPLACEMENT, SCALE
    public static float animationChangePerMs;
    public static float fadeChangePerMs;

    public static int worldCurvature;
    public static int configVersion;

    public static Curve animationCurve;
    public static FadeType fadeType;
    public static AnimationType animationType;

    static {
        addEntry(new ConfigEntry<Integer>(CONFIG_VERSION, CONFIG_VERSION_KEY, Type.INTEGER))
            .addListener((o) -> configVersion = o);
        addEntry(new ConfigEntry<Integer>(Curve.EASE_OUT.ordinal(), ANIMATION_CURVE_KEY, Type.INTEGER))
            .addListener((o) -> animationCurve = Curve.values()[MathUtils.clamp(o, 0,
                Curve.values().length - 1)]);
        addEntry(new ConfigEntry<Integer>(FadeType.FULL.ordinal(), FADE_TYPE_KEY, Type.INTEGER))
            .addListener((o) -> fadeType = FadeType.values()[MathUtils.clamp(o, 0,
                FadeType.values().length - 1)]);
        addEntry(new ConfigEntry<Integer>(AnimationType.FULL.ordinal(), ANIMATION_TYPE_KEY, Type.INTEGER))
            .addListener((o) -> animationType = AnimationType.values()[MathUtils.clamp(o, 0,
                AnimationType.values().length - 1)]);
        addEntry(new ConfigEntry<Integer>(16384, CURVATURE_KEY, Type.INTEGER))
            .addListener((o) -> worldCurvature = o);

        addEntry(new ConfigEntryDoubleLimitable(MIN_FADE_TIME, MAX_FADE_TIME, 0.75, FADE_TIME_KEY))
            .addListener((o) -> fadeChangePerMs = fadeChangeFromSeconds(o));
        addEntry(new ConfigEntryDoubleLimitable(MIN_ANIMATION_TIME, MAX_ANIMATION_TIME, 2.56, ANIMATION_TIME_KEY))
            .addListener((o) -> animationChangePerMs = animationChangeFromSeconds(o));
        addEntry(new ConfigEntryDoubleLimitable(MIN_ANIMATION_OFFSET, MAX_ANIMATION_OFFSET, -64, ANIMATION_OFFSET_KEY))
            .addListener((o) -> animationOffset = o.floatValue());
        addEntry(new ConfigEntryDoubleLimitable(MIN_ANIMATION_ANGLE, MAX_ANIMATION_ANGLE, 0, ANIMATION_ANGLE_KEY))
            .addListener((o) -> animationAngle = o.floatValue());
        addEntry(new ConfigEntryDoubleLimitable(MIN_ANIMATION_FACTOR, MAX_ANIMATION_FACTOR, 1, ANIMATION_FACTOR_KEY))
            .addListener((o) -> animationFactor = o.floatValue());

        addEntry(new ConfigEntry<Boolean>(true, MOD_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isModEnabled = o);
        addEntry(new ConfigEntry<Boolean>(true, FADE_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isFadeEnabled = o);
        addEntry(new ConfigEntry<Boolean>(false, ANIMATION_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isAnimationEnabled = o);
        addEntry(new ConfigEntry<Boolean>(false, CURVATURE_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isCurvatureEnabled = o);
        addEntry(new ConfigEntry<Boolean>(true, UPDATE_NOTIFIER_ENABLED_KEY, Type.BOOLEAN))
            .addListener((o) -> isUpdateNotifierEnabled = o);
        addEntry(new ConfigEntry<Boolean>(true, SHOW_MOD_TAB_IN_SETTINGS_KEY, Type.BOOLEAN))
            .addListener((o) -> showModTabInSettings = o);
        addEntry(new ConfigEntry<Boolean>(true, ANIMATE_NEAR_PLAYER_KEY, Type.BOOLEAN))
            .addListener((o) -> animateNearPlayer = o);
        addEntry(new ConfigEntry<Boolean>(true, FADE_NEAR_PLAYER_KEY, Type.BOOLEAN))
            .addListener((o) -> fadeNearPlayer = o);
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
        return (double) (1 / animationChangePerMs / 1E+3);
    }

    public static void load() {
        configFile = new File(Services.PLATFORM.getConfigDirectory(), "chunksfadein.properties");
        Toml toml = new Toml();

        try {
            if (!configFile.exists())
                configFile.createNewFile();

            toml.read(configFile);
        } catch (Exception e) {
        }

        for (ConfigEntry<?> entry : entries.values())
            entry.load(toml);

        if (configVersion < 3)
            setDouble(ANIMATION_OFFSET_KEY, -getDouble(ANIMATION_OFFSET_KEY));

        setInteger(CONFIG_VERSION_KEY, CONFIG_VERSION);
        save();
    }

    public static void save() {
        String string = "";

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

    public static int setInteger(String key, Integer value) {
        ConfigEntry<Integer> entry = get(key);

        return entry.set(value);
    }

    public static boolean setBoolean(String key, Boolean value) {
        ConfigEntry<Boolean> entry = get(key);

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
