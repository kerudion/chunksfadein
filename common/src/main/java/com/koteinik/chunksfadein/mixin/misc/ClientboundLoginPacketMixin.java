package com.koteinik.chunksfadein.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.core.UpdateNotifier;

import net.minecraft.network.protocol.game.ClientboundLoginPacket;

@Mixin(ClientboundLoginPacket.class)
public class ClientboundLoginPacketMixin {
	@Inject(method = "handle", at = @At("RETURN"))
	private void modifyHandle(CallbackInfo ci) {
		UpdateNotifier.checkAndNotify();
	}
}
