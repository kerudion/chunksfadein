package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Curves;
import com.koteinik.chunksfadein.core.FadeTypes;
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

        addDrawableChild(GuiUtils.choiceButton(
            this, -1, -4,
            MOD_ENABLED, Config.MOD_ENABLED_KEY, isInGame ? null : Config.isModEnabled, isInGame ? MOD_ENABLED_TOOLTIP : null));
        addDrawableChild(GuiUtils.toggledButton(
            this, 1, -4,
            UPDATE_NOTIFIER_ENABLED, Config.UPDATE_NOTIFIER_ENABLED_KEY));
        addDrawableChild(GuiUtils.toggledButton(
            this, 0, -3,
            MOD_BUTTON_ENABLED, Config.SHOW_MOD_BUTTON_IN_SETTINGS_KEY, CompatibilityHook.isModMenuLoaded ? null : true, MOD_BUTTON_TOOLTIP));
        addDrawableChild(GuiUtils.choiceButton(
            this, 0, -2,
            FADE_ENABLED, Config.FADE_ENABLED_KEY, null, irisWarningTooltip));
        addDrawableChild(GuiUtils.button(
            this, -1, -1,
            () -> GuiUtils.coloredFormatted(FADE_TYPE, "§e", Config.fadeType.getTranslation()),
            () -> {
                Integer next = Config.fadeType.ordinal() + 1;

                if (next > FadeTypes.values().length - 1)
                    next = 0;

                Config.setInteger(Config.FADE_TYPE_KEY, next);

                needReload = true;
            }));
        addDrawableChild(addDrawableChild(
            GuiUtils.slider(this, 1, -1,
                () -> Config.secondsFromFadeChange() / Config.MAX_FADE_TIME,
                () -> Config.secondsFromFadeChange(), FADE_TIME, Config.FADE_TIME_KEY,
                Config.MAX_FADE_TIME))
                    .attachResetButton(Config.FADE_TIME_KEY));

        addDrawableChild(GuiUtils.choiceButton(
            this, 0, 0,
            ANIMATION_ENABLED, Config.ANIMATION_ENABLED_KEY));
        addDrawableChild(GuiUtils.button(
            this, -1, 1,
            () -> GuiUtils.coloredFormatted(ANIMATION_CURVE, "§e", Config.animationCurve.getTranslation()),
            () -> {
                Integer next = Config.animationCurve.ordinal() + 1;

                if (next > Curves.values().length - 1)
                    next = 0;

                Config.setInteger(Config.ANIMATION_CURVE_KEY, next);
            }));
        addDrawableChild(addDrawableChild(
            GuiUtils.slider(this, 1, 1,
                () -> Config.animationInitialOffset / Config.MAX_ANIMATION_OFFSET,
                () -> Config.animationInitialOffset, ANIMATION_START, Config.ANIMATION_OFFSET_KEY,
                Config.MAX_ANIMATION_OFFSET))
                    .attachResetButton(Config.ANIMATION_OFFSET_KEY));
        addDrawableChild(GuiUtils.choiceButton(
            this, -1, 2,
            ANIMATE_NEAR_PLAYER, Config.ANIMATE_NEAR_PLAYER_KEY));
        addDrawableChild(addDrawableChild(
            GuiUtils.slider(this, 1, 2,
                () -> Config.secondsFromAnimationChange() / Config.MAX_ANIMATION_TIME,
                () -> Config.secondsFromAnimationChange(), ANIMATION_TIME, Config.ANIMATION_TIME_KEY,
                Config.MAX_ANIMATION_TIME))
                    .attachResetButton(Config.ANIMATION_TIME_KEY));

        addDrawableChild(GuiUtils.button(this, 0, 3,
            () -> GuiUtils.choiceText(WORLD_CURVATURE_ENABLED, Config.getBoolean(Config.CURVATURE_ENABLED_KEY)),
            () -> {
                Config.flipBoolean(Config.CURVATURE_ENABLED_KEY);
                needReload = true;
            }, null, irisWarningTooltip));
        addDrawableChild(addDrawableChild(
            GuiUtils.slider(this, 0, 4,
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
                15))
                    .attachResetButton(Config.CURVATURE_KEY));

        addDrawableChild(GuiUtils.doneButton(this));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 20,
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
