package com.koteinik.chunksfadein;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import net.minecraft.util.math.Vec3d;

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

    public static Vec3d abs(Vec3d in) {
        return new Vec3d(Math.abs(in.x), Math.abs(in.y), Math.abs(in.z));
    }

    public static int floor(float x) {
        final float f = x % 1f;
        if (x < 0f && f != 0f)
            x--;

        return f >= 0.5f ? (int) (x - 0.5f) : (int) x;
    }

    public static int chunkIdFromGlobal(int x, int y, int z) {
        int rX = x & (RenderRegion.REGION_WIDTH - 1);
        int rY = y & (RenderRegion.REGION_HEIGHT - 1);
        int rZ = z & (RenderRegion.REGION_LENGTH - 1);

        return RenderRegion.getChunkIndex(rX, rY, rZ);
    }

    public static boolean chunkInRange(int aX, int aY, int aZ, int bX, int bY, int bZ, int radius) {
        if (Math.abs(aX - bX) > radius)
            return false;
        if (Math.abs(aY - bY) > radius)
            return false;
        if (Math.abs(aZ - bZ) > radius)
            return false;

        return true;
    }

    public static float sqrt(float f) {
        return (float) Math.sqrt((double) f);
    }
}
