package com.koteinik.chunksfadein.mixin.misc;

import java.util.concurrent.CompletableFuture;

import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.OpenSettingsButton;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.TranslatableTextContent;

@Mixin(value = OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    @Shadow
    private ThreePartsLayoutWidget layout;

    protected OptionsScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void modifyInit(CallbackInfo ci) {
        if (!Config.showModButtonInSettings && CompatibilityHook.isModMenuLoaded)
            return;

        CompletableFuture<Vector2i> coordsFuture = new CompletableFuture<>();
        layout.forEachChild(w -> {
            if (!(w instanceof ButtonWidget btn))
                return;
            if (!(btn.getMessage().getContent() instanceof TranslatableTextContent translatable))
                return;
            if (!translatable.getKey().equals("options.video"))
                return;

            coordsFuture.complete(new Vector2i(w.getX() - 26, w.getY()));
        });

        Vector2i coords = coordsFuture.getNow(new Vector2i(width / 2 - 154 - 26, height / 6 + 72 - 6));

        addDrawableChild(new OpenSettingsButton(this, client, coords.x, coords.y));
    }
}
