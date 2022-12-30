package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.core.ChunkAppearedLink;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(value = WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk {
    public WorldChunkMixin() {
        super(null, null, null, null, 0, null, null);
    }

    @Shadow
    World world;

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;isEmpty()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifySetBlockState(BlockPos pos, BlockState state, boolean moved,
            CallbackInfoReturnable<BlockState> cir, int i, ChunkSection chunkSection) {
        if (!chunkSection.isEmpty())
            return;

        ChunkPos chunkPos = getPos();
        ChunkAppearedLink.completeChunkFade(chunkPos.x, MathUtils.floor(i / 16), chunkPos.z, true);
    }
}
