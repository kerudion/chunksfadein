package com.koteinik.chunksfadein;

public class MathUtils {
    public static double clamp(double value, double min, double max) {
        return value < min ? min : value > max ? max : value;
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    public static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    public static float pow(float in, int times) {
        for (int i = -1; i < times; i++)
            in *= in;

        return in;
    }

    public static float abs(float in) {
        return in >= 0f ? in : -in;
    }

    public static int floor(float x) {
        int y;

        if (x >= Float.MAX_VALUE || x <= -Float.MAX_VALUE)
            return (int) x;

        y = (int) x;
        if (x < 0 && y != x)
            y--;

        if (y == 0)
            return 0;

        return y;
    }
}
