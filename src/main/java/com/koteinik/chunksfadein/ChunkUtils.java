package com.koteinik.chunksfadein;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;

public class ChunkUtils {
    public static ChunkSection getChunkOn(World world, ChunkSectionPos pos) {
        return getChunkOn(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static ChunkSection getChunkOn(World world, ChunkPos pos, int y) {
        return getChunkOn(world, pos.x, y, pos.z);
    }

    public static ChunkSection getChunkOn(World world, int x, int y, int z) {
        ChunkSection[] arr = world.getChunk(x, z).getSectionArray();
        int sectionY = world.sectionCoordToIndex(y);

        if (sectionY < arr.length && sectionY >= 0)
            return arr[sectionY];
        else
            return null;
    }
}
