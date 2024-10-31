package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.AnimationType;
import com.koteinik.chunksfadein.core.Curve;
import com.koteinik.chunksfadein.core.FadeType;
import com.koteinik.chunksfadein.gui.components.CFIButton;
import com.koteinik.chunksfadein.gui.components.CFIListWidget;
import com.koteinik.chunksfadein.gui.components.CFISlider;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen extends Screen {
    public static final Component YES = GuiUtils.text("settings.chunksfadein.yes");
    public static final Component NO = GuiUtils.text("settings.chunksfadein.no");
    public static final Component ON = GuiUtils.text("settings.chunksfadein.on");
    public static final Component OFF = GuiUtils.text("settings.chunksfadein.off");
    public static final Component RESET = GuiUtils.text("settings.chunksfadein.reset");
    public static final Component TITLE = GuiUtils.text("settings.chunksfadein.title");
    public static final String MOD_ENABLED = "settings.chunksfadein.mod_enabled";
    public static final Component MOD_ENABLED_TOOLTIP = GuiUtils.text("settings.chunksfadein.mod_enabled_tooltip");
    public static final String UPDATE_NOTIFIER_ENABLED = "settings.chunksfadein.update_notifier_enabled";
    public static final String MOD_TAB_ENABLED = "settings.chunksfadein.mod_tab_enabled";
    public static final Component MOD_TAB_TOOLTIP = GuiUtils.text("settings.chunksfadein.mod_tab_tooltip");
    public static final String FADE_ENABLED = "settings.chunksfadein.fade_enabled";
    public static final String FADE_TYPE = "settings.chunksfadein.fade_type";
    public static final String FADE_TIME = "settings.chunksfadein.fade_time";
    public static final String FADE_NEAR_PLAYER = "settings.chunksfadein.fade_near_player";
    public static final String ANIMATION_ENABLED = "settings.chunksfadein.animation_enabled";
    public static final String ANIMATION_TYPE = "settings.chunksfadein.animation_type";
    public static final String ANIMATION_CURVE = "settings.chunksfadein.animation_curve";
    public static final String ANIMATION_START = "settings.chunksfadein.animation_start";
    public static final String ANIMATION_ANGLE = "settings.chunksfadein.animation_angle";
    public static final String ANIMATION_FACTOR = "settings.chunksfadein.animation_factor";
    public static final String ANIMATE_NEAR_PLAYER = "settings.chunksfadein.animate_near_player";
    public static final String ANIMATION_TIME = "settings.chunksfadein.animation_time";
    public static final String WORLD_CURVATURE_ENABLED = "settings.chunksfadein.world_curvature_enabled";
    public static final String WORLD_CURVATURE = "settings.chunksfadein.world_curvature";
    public static final String IRIS_WARNING = "settings.chunksfadein.iris_warning";

    private final Screen parent;
    private boolean needReload = false;
    private CFIListWidget list = null;

    public SettingsScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    public void init() {
        rebuildList();
        addRenderableWidget(GuiUtils.doneButton(this));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(font, title, width / 2, 12,
            16777215 | 255 << 24);
    }

    @Override
    public void onClose() {
        Config.save();

        if (needReload)
            ShaderUtils.reloadWorldRenderer();

        minecraft.setScreen(parent);
    }

    private void rebuildList() {
        if (list != null)
            removeWidget(list);

        addRenderableWidget(list = buildList());
    }

    private CFIListWidget buildList() {
        Component irisWarningTooltip = CompatibilityHook.isIrisShaderPackInUse() ? GuiUtils.text(IRIS_WARNING) : null;

        CFIListWidget list = new CFIListWidget(minecraft, this, width, height - 64, 28);

        CFIButton modEnabled = GuiUtils.choiceButton(
            MOD_ENABLED, Config.MOD_ENABLED_KEY,
            () -> needReload = true);
        CFIButton updateNotifier = GuiUtils.toggledButton(
            UPDATE_NOTIFIER_ENABLED,
            Config.UPDATE_NOTIFIER_ENABLED_KEY);
        list.add(modEnabled, updateNotifier);

        CFIButton buttonInSettings = GuiUtils.toggledButton(
            MOD_TAB_ENABLED, Config.SHOW_MOD_TAB_IN_SETTINGS_KEY,
            CompatibilityHook.isModMenuLoaded ? null : true,
            CompatibilityHook.isModMenuLoaded ? null : MOD_TAB_TOOLTIP);
        list.add(buttonInSettings);

        CFIButton fadeEnabled = GuiUtils.choiceButton(
            FADE_ENABLED, Config.FADE_ENABLED_KEY,
            null, irisWarningTooltip, () -> needReload = true);
        CFIButton fadeType = GuiUtils.button(
            () -> GuiUtils.coloredFormatted(FADE_TYPE, "§e", Config.fadeType.getTranslation()),
            () -> {
                Integer next = Config.fadeType.ordinal() + 1;

                if (next >= FadeType.values().length)
                    next = 0;

                Config.setInteger(Config.FADE_TYPE_KEY, next);

                needReload = true;
            });
        CFISlider fadeTime = GuiUtils.slider(
            () -> Config.secondsFromFadeChange() / Config.MAX_FADE_TIME,
            () -> Config.secondsFromFadeChange(), FADE_TIME, Config.FADE_TIME_KEY,
            Config.MAX_FADE_TIME);
        CFIButton fadeNearPlayer = GuiUtils.choiceButton(FADE_NEAR_PLAYER, Config.FADE_NEAR_PLAYER_KEY);
        list.add(fadeEnabled);
        list.add(fadeType, fadeTime, fadeTime.makeResetButton(Config.FADE_TIME_KEY));
        list.add(fadeNearPlayer);

        CFIButton animationEnabled = GuiUtils.choiceButton(ANIMATION_ENABLED, Config.ANIMATION_ENABLED_KEY, () -> needReload = true);
        CFIButton animationCurve = GuiUtils.button(
            () -> GuiUtils.coloredFormatted(ANIMATION_CURVE, "§e", Config.animationCurve.getTranslation()),
            () -> {
                Integer next = Config.animationCurve.ordinal() + 1;

                if (next >= Curve.values().length)
                    next = 0;

                Config.setInteger(Config.ANIMATION_CURVE_KEY, next);
            });
        CFIButton animationType = GuiUtils.button(
            () -> GuiUtils.coloredFormatted(ANIMATION_TYPE, "§e", Config.animationType.getTranslation()),
            () -> {
                Integer next = Config.animationType.ordinal() + 1;

                if (next >= AnimationType.values().length)
                    next = 0;

                Config.setInteger(Config.ANIMATION_TYPE_KEY, next);

                rebuildList();
                needReload = true;
            });
        CFISlider animationOffset = GuiUtils.slider(
            () -> (Config.animationOffset - Config.MIN_ANIMATION_OFFSET)
                / (Config.MAX_ANIMATION_OFFSET - Config.MIN_ANIMATION_OFFSET),
            () -> Config.animationOffset,
            ANIMATION_START,
            value -> Config.setDouble(Config.ANIMATION_OFFSET_KEY, MathUtils.round(value + Config.MIN_ANIMATION_OFFSET)),
            Config.MAX_ANIMATION_OFFSET - Config.MIN_ANIMATION_OFFSET);
        CFISlider animationAngle = GuiUtils.slider(
            () -> Config.animationAngle / Config.MAX_ANIMATION_ANGLE,
            () -> Config.animationAngle,
            ANIMATION_ANGLE,
            value -> Config.setDouble(Config.ANIMATION_ANGLE_KEY, MathUtils.round(value)),
            Config.MAX_ANIMATION_ANGLE);
        CFISlider animationFactor = GuiUtils.slider(() -> Config.animationFactor,
            () -> Config.animationFactor, ANIMATION_FACTOR,
            Config.ANIMATION_FACTOR_KEY, 1);
        CFISlider animationTime = GuiUtils.slider(
            () -> Config.secondsFromAnimationChange() / Config.MAX_ANIMATION_TIME,
            () -> Config.secondsFromAnimationChange(), ANIMATION_TIME, Config.ANIMATION_TIME_KEY,
            Config.MAX_ANIMATION_TIME);
        CFIButton animateNearPlayer = GuiUtils.choiceButton(ANIMATE_NEAR_PLAYER, Config.ANIMATE_NEAR_PLAYER_KEY);
        list.add(animationEnabled);
        if (Config.animationType == AnimationType.FULL) {
            list.add(animationType, animationOffset, animationOffset.makeResetButton(Config.ANIMATION_OFFSET_KEY));
            list.add(animationCurve, animationAngle, animationAngle.makeResetButton(Config.ANIMATION_ANGLE_KEY));
            list.add(animateNearPlayer, animationTime, animationTime.makeResetButton(Config.ANIMATION_TIME_KEY));
        } else {
            list.add(animationType, animationFactor, animationFactor.makeResetButton(Config.ANIMATION_FACTOR_KEY));
            list.add(animationCurve, animationTime, animationTime.makeResetButton(Config.ANIMATION_TIME_KEY));
            list.add(animateNearPlayer);
        }

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

        return list;
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
