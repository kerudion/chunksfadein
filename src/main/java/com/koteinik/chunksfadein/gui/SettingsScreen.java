package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Curves;
import com.koteinik.chunksfadein.core.FadeTypes;
import com.koteinik.chunksfadein.gui.components.CFIButton;
import com.koteinik.chunksfadein.gui.components.CFIListWidget;
import com.koteinik.chunksfadein.gui.components.CFISlider;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;

public class SettingsScreen extends Screen {
    public static final Text YES = GuiUtils.text("settings.chunksfadein.yes");
    public static final Text NO = GuiUtils.text("settings.chunksfadein.no");
    public static final Text ON = GuiUtils.text("settings.chunksfadein.on");
    public static final Text OFF = GuiUtils.text("settings.chunksfadein.off");
    public static final Text RESET = GuiUtils.text("settings.chunksfadein.reset");
    public static final Text TITLE = GuiUtils.text("settings.chunksfadein.title");
    public static final String MOD_ENABLED = "settings.chunksfadein.mod_enabled";
    public static final Text MOD_ENABLED_TOOLTIP = GuiUtils.text("settings.chunksfadein.mod_enabled_tooltip");
    public static final String UPDATE_NOTIFIER_ENABLED = "settings.chunksfadein.update_notifier_enabled";
    public static final String MOD_BUTTON_ENABLED = "settings.chunksfadein.mod_button_enabled";
    public static final Text MOD_BUTTON_TOOLTIP = GuiUtils.text("settings.chunksfadein.mod_button_tooltip");
    public static final String FADE_ENABLED = "settings.chunksfadein.fade_enabled";
    public static final String FADE_TYPE = "settings.chunksfadein.fade_type";
    public static final String FADE_TIME = "settings.chunksfadein.fade_time";
    public static final String FADE_NEAR_PLAYER = "settings.chunksfadein.fade_near_player";
    public static final String ANIMATION_ENABLED = "settings.chunksfadein.animation_enabled";
    public static final String ANIMATION_CURVE = "settings.chunksfadein.animation_curve";
    public static final String ANIMATION_START = "settings.chunksfadein.animation_start";
    public static final String ANIMATE_NEAR_PLAYER = "settings.chunksfadein.animate_near_player";
    public static final String ANIMATION_TIME = "settings.chunksfadein.animation_time";
    public static final String WORLD_CURVATURE_ENABLED = "settings.chunksfadein.world_curvature_enabled";
    public static final String WORLD_CURVATURE = "settings.chunksfadein.world_curvature";
    public static final String IRIS_WARNING = "settings.chunksfadein.iris_warning";

    private boolean needReload = false;

    public SettingsScreen(Screen parent) {
        super(TITLE);
    }

    @Override
    public void init() {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();

        Text irisWarningTooltip = CompatibilityHook.isIrisShaderPackInUse() ? GuiUtils.text(IRIS_WARNING) : null;
        boolean isInGame = networkHandler == null || !networkHandler.getConnection().isOpen();

        CFIListWidget list = new CFIListWidget(client, this, width, height - 64, 28);

        CFIButton modEnabled = GuiUtils.choiceButton(
            MOD_ENABLED, Config.MOD_ENABLED_KEY,
            isInGame ? null : Config.isModEnabled,
            isInGame ? MOD_ENABLED_TOOLTIP : null);
        CFIButton updateNotifier = GuiUtils.toggledButton(
            UPDATE_NOTIFIER_ENABLED,
            Config.UPDATE_NOTIFIER_ENABLED_KEY);
        list.add(modEnabled, updateNotifier);

        CFIButton buttonInSettings = GuiUtils.toggledButton(
            MOD_BUTTON_ENABLED, Config.SHOW_MOD_BUTTON_IN_SETTINGS_KEY,
            CompatibilityHook.isModMenuLoaded ? null : true,
            CompatibilityHook.isModMenuLoaded ? null : MOD_BUTTON_TOOLTIP);
        list.add(buttonInSettings);

        CFIButton fadeEnabled = GuiUtils.choiceButton(
            FADE_ENABLED, Config.FADE_ENABLED_KEY,
            null, irisWarningTooltip);
        CFIButton fadeType = GuiUtils.button(
            () -> GuiUtils.coloredFormatted(FADE_TYPE, "§e", Config.fadeType.getTranslation()),
            () -> {
                Integer next = Config.fadeType.ordinal() + 1;

                if (next >= FadeTypes.values().length)
                    next = 0;

                Config.setInteger(Config.FADE_TYPE_KEY, next);

                needReload = true;
            });
        CFISlider fadeTime = GuiUtils.slider(
            () -> Config.secondsFromFadeChange() / Config.MAX_FADE_TIME,
            () -> Config.secondsFromFadeChange(), FADE_TIME, Config.FADE_TIME_KEY,
            Config.MAX_FADE_TIME);
        list.add(fadeEnabled);
        list.add(fadeType, fadeTime, fadeTime.makeResetButton(Config.FADE_TIME_KEY));

        CFIButton fadeNearPlayer = GuiUtils.choiceButton(FADE_NEAR_PLAYER, Config.FADE_NEAR_PLAYER_KEY);
        list.add(fadeNearPlayer);

        CFIButton animationEnabled = GuiUtils.choiceButton(ANIMATION_ENABLED, Config.ANIMATION_ENABLED_KEY);
        CFIButton animationCurve = GuiUtils.button(
            () -> GuiUtils.coloredFormatted(ANIMATION_CURVE, "§e", Config.animationCurve.getTranslation()),
            () -> {
                Integer next = Config.animationCurve.ordinal() + 1;

                if (next >= Curves.values().length)
                    next = 0;

                Config.setInteger(Config.ANIMATION_CURVE_KEY, next);
            });
        CFISlider animationOffset = GuiUtils.slider(
            () -> Config.animationInitialOffset / Config.MAX_ANIMATION_OFFSET,
            () -> Config.animationInitialOffset, ANIMATION_START, Config.ANIMATION_OFFSET_KEY,
            Config.MAX_ANIMATION_OFFSET);
        CFIButton animateNearPlayer = GuiUtils.choiceButton(ANIMATE_NEAR_PLAYER, Config.ANIMATE_NEAR_PLAYER_KEY);
        CFISlider animationTime = GuiUtils.slider(
            () -> Config.secondsFromAnimationChange() / Config.MAX_ANIMATION_TIME,
            () -> Config.secondsFromAnimationChange(), ANIMATION_TIME, Config.ANIMATION_TIME_KEY,
            Config.MAX_ANIMATION_TIME);
        list.add(animationEnabled);
        list.add(animationCurve, animationOffset, animationOffset.makeResetButton(Config.ANIMATION_OFFSET_KEY));
        list.add(animateNearPlayer, animationTime, animationTime.makeResetButton(Config.ANIMATION_TIME_KEY));

        CFIButton curvatureEnabled = GuiUtils.button(
            () -> GuiUtils.choiceText(WORLD_CURVATURE_ENABLED,
                Config.getBoolean(Config.CURVATURE_ENABLED_KEY)),
            () -> {
                Config.flipBoolean(Config.CURVATURE_ENABLED_KEY);
                needReload = true;
            }, null, irisWarningTooltip);
        CFISlider curvatureFactor = GuiUtils.slider(
            () -> {
                for (int i = 0; i < 16; i++)
                    if (Config.worldCurvature == getCurvatureValue(i))
                        return (double) i / 16;

                return 0;
            },
            () -> Config.worldCurvature, WORLD_CURVATURE,
            (v) -> {
                Config.setInteger(Config.CURVATURE_KEY, getCurvatureValue((int) Math.round(v)));

                needReload = true;
            },
            15);
        list.add(curvatureEnabled, curvatureFactor, curvatureFactor.makeResetButton(Config.CURVATURE_KEY));

        addDrawableChild(list);

        addDrawableChild(GuiUtils.doneButton(this));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12,
            16777215 | 255 << 24);
    }

    @Override
    public void removed() {
        super.removed();
        Config.save();

        if (needReload)
            ShaderUtils.reloadWorldRenderer();
    }

    private static int getCurvatureValue(int value) {
        int curvatureValue;
        if (value < 8)
            curvatureValue = (int) -Math.pow(2, 16 - value);
        else
            curvatureValue = (int) Math.pow(2, value + 1);

        return curvatureValue;
    }
}
