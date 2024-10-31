package com.koteinik.chunksfadein;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ChunkUtils {
    public static LevelChunkSection getChunkOn(Level world, SectionPos pos) {
        return getChunkOn(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static LevelChunkSection getChunkOn(Level world, ChunkPos pos, int y) {
        return getChunkOn(world, pos.x, y, pos.z);
    }

    public static LevelChunkSection getChunkOn(Level world, int x, int y, int z) {
        LevelChunkSection[] arr = world.getChunk(x, z).getSections();
        int sectionY = world.getSectionIndexFromSectionY(y);

        if (sectionY < arr.length && sectionY >= 0)
            return arr[sectionY];
        else
            return null;
    }
}
