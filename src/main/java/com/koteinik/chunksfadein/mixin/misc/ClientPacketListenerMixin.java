package com.koteinik.chunksfadein.mixin.misc;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Mixin(value = LoginSuccessS2CPacket.class)
public class ClientPacketListenerMixin {
    @Inject(method = "apply", at = @At(value = "TAIL"))
    @SuppressWarnings("resource")
    private void modifyApply(ClientLoginPacketListener clientLoginPacketListener, CallbackInfo ci) {
        new Thread(() -> {
            ModrinthVersion latestVersion = ModrinthApi.getLatestModVersion();

            if (isNewerVersion(latestVersion)) {
                List<Text> textList = new ArrayList<>();
                textList.add(Text.of("§7New version of §2Chunks fade in §7is available!"));

                Style linkStyle = Style.EMPTY.withClickEvent(
                        new ClickEvent(net.minecraft.text.ClickEvent.Action.OPEN_URL, latestVersion.downloadUrl));

                textList.add(Text.of("§7v" + latestVersion.version.getFriendlyString() + "§r§7 changelog:"));
                textList.add(Text.of("§7" + latestVersion.changelog));
                textList.addAll(Text.of("§7§nClick to download").getWithStyle(linkStyle));

                for (Text text : textList)
                    MinecraftClient.getInstance().player.sendMessage(text);
            }
        }).start();
    }

    private static boolean isNewerVersion(ModrinthVersion modrinthVersion) {
        return FabricLoader.getInstance().getModContainer("chunksfadein").get().getMetadata()
                .getVersion().compareTo(modrinthVersion.version) < 0;
    }
}
