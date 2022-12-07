package com.koteinik.chunksfadein;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.koteinik.chunksfadein.hooks.IrisApiHook;
import com.moandjiezana.toml.Toml;

import net.fabricmc.loader.api.FabricLoader;

public class Config {
    private static final String FADE_TIME_KEY = "fade-time";
    private static final String ENABLED_KEY = "enabled";
    private static File configFile = null;

    public static final double DEFAULT_FADE_TIME = 0.64;
    public static final int MAX_FADE_TIME = 3;
    public static final double MIN_FADE_TIME = 0.01;
    public static float fadeCoeffPerMs = 0f;

    public static boolean isModEnabled;

    public static void setFadeCoeffFromSeconds(double seconds) {
        final double secondsInMs = seconds * 1000;

        fadeCoeffPerMs = (float) (1 / secondsInMs);
    }

    public static double getSecondsFromFadeCoeff() {
        return 1 / fadeCoeffPerMs / 1000;
    }

    public static boolean needToTurnOff() {
        return IrisApiHook.isShaderPackInUse() || !isModEnabled;
    }

    public static void loadConfigFile() {
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(),
                "chunksfadein.properties");
    }

    public static void loadConfig() {
        try {
            Double fadeTime = null;
            Boolean modEnabled = null;
            if (configFile.exists()) {
                Toml toml = new Toml().read(configFile);
                fadeTime = toml.getDouble(FADE_TIME_KEY);
                modEnabled = toml.getBoolean(ENABLED_KEY);
            }

            if (fadeTime == null)
                fadeTime = DEFAULT_FADE_TIME;

            if (modEnabled == null)
                modEnabled = true;

            fadeTime = MathUtils.clamp(fadeTime, MIN_FADE_TIME, MAX_FADE_TIME);

            setFadeCoeffFromSeconds(fadeTime);
            isModEnabled = modEnabled;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Failed to load config! Resetting to defalt values...");

            setFadeCoeffFromSeconds(DEFAULT_FADE_TIME);
        }
    }

    public static void saveConfig() {
        try {
            if (!configFile.exists())
                configFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(FADE_TIME_KEY + " = " + getSecondsFromFadeCoeff() + "\n"
                    + ENABLED_KEY + " = " + isModEnabled);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Failed to save config! If this is an error, please create an issue on github!");
        }
    }
}
