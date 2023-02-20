package com.koteinik.chunksfadein.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.gui.OpenSettingsButton;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;

@Mixin(value = OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void modifyInit(CallbackInfo ci) {
        addDrawableChild(new OpenSettingsButton(this, client, width, height));
    }
}
