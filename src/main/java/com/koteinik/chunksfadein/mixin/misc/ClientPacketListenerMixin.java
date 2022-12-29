package com.koteinik.chunksfadein.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.core.ModrinthApi;
import com.koteinik.chunksfadein.core.ModrinthApi.ModrinthVersion;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.text.Text;

@Mixin(value = LoginSuccessS2CPacket.class)
public class ClientPacketListenerMixin {
    @Inject(method = "apply", at = @At(value = "TAIL"))
    @SuppressWarnings("resource")
    private void modifyApply(ClientLoginPacketListener clientLoginPacketListener, CallbackInfo ci) {
        new Thread(() -> {
            ModrinthVersion latestVersion = ModrinthApi.getLatestModVersion();

            if (isNewerVersion(latestVersion)) {
                String textStr = "§7New version of §2Chunks fade in §7is available!\n";
                textStr += "§2v" + latestVersion.version.getFriendlyString() + " §7changelog:\n";
                textStr += "§7" + latestVersion.changelog;

                Text text = Text.of(textStr);

                MinecraftClient.getInstance().player.sendMessage(text);
            }
        }).start();
    }

    private static boolean isNewerVersion(ModrinthVersion modrinthVersion) {
        return FabricLoader.getInstance().getModContainer("chunksfadein").get().getMetadata()
                .getVersion().compareTo(modrinthVersion.version) < 0;
    }
}
