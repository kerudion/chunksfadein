package com.koteinik.chunksfadein;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.moandjiezana.toml.Toml;

import net.fabricmc.loader.api.FabricLoader;

public class Config {
    private static final String FADE_TIME_KEY = "fade-time";
    private static final double DEFAULT_FADE_TIME = 0.64;
    public static float fadeCoeffPerMs = 0f;
    private static File configFile = null;

    public static void setFadeCoeffFromSeconds(double seconds) {
        final double secondsInMs = seconds * 1000;

        fadeCoeffPerMs = (float) (1 / secondsInMs);
    }

    public static double getSecondsFromFadeCoeff() {
        return 1 / fadeCoeffPerMs / 1000;
    }

    public static void loadConfigFile() {
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(),
                "chunksfadein.properties");
    }

    public static void loadConfig() {
        try {
            Double fadeTime = null;
            if (configFile.exists()) {
                Toml toml = new Toml().read(configFile);
                fadeTime = toml.getDouble(FADE_TIME_KEY);
            }

            if (fadeTime == null)
                fadeTime = DEFAULT_FADE_TIME;

            fadeTime = MathUtils.clamp(fadeTime, 0, 3);

            setFadeCoeffFromSeconds(fadeTime);
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
            writer.write(FADE_TIME_KEY + " = " + getSecondsFromFadeCoeff());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Failed to save config! If this is an error, please create an issue on github!");
        }
    }
}
