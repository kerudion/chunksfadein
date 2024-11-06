package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.AnimationType;
import com.koteinik.chunksfadein.core.Curve;
import com.koteinik.chunksfadein.core.FadeType;
import com.koteinik.chunksfadein.gui.components.CFIButton;
import com.koteinik.chunksfadein.gui.components.CFIButton.CFIButtonBuilder;
import com.koteinik.chunksfadein.gui.components.CFISlider.CFISliderBuilder;
import com.koteinik.chunksfadein.gui.components.CFIListWidget;
import com.koteinik.chunksfadein.gui.components.CFISlider;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SettingsScreen extends Screen {
    public static final MutableComponent YES = Component.translatable("settings.chunksfadein.yes");
    public static final MutableComponent NO = Component.translatable("settings.chunksfadein.no");
    public static final MutableComponent ON = Component.translatable("settings.chunksfadein.on");
    public static final MutableComponent OFF = Component.translatable("settings.chunksfadein.off");
    public static final MutableComponent RESET = Component.translatable("settings.chunksfadein.reset");
    public static final MutableComponent TITLE = Component.translatable("settings.chunksfadein.title");
    public static final MutableComponent UNITS_SECONDS = Component.translatable("settings.chunksfadein.units.seconds");
    public static final MutableComponent UNITS_BLOCKS = Component.translatable("settings.chunksfadein.units.blocks");
    public static final MutableComponent UNITS_DEGREES = Component.translatable("settings.chunksfadein.units.degrees");
    public static final String MOD_ENABLED = "settings.chunksfadein.mod_enabled";
    public static final MutableComponent MOD_ENABLED_TOOLTIP = Component.translatable("settings.chunksfadein.mod_enabled_tooltip");
    public static final String UPDATE_NOTIFIER_ENABLED = "settings.chunksfadein.update_notifier_enabled";
    public static final String MOD_TAB_ENABLED = "settings.chunksfadein.mod_tab_enabled";
    public static final MutableComponent MOD_TAB_TOOLTIP = Component.translatable("settings.chunksfadein.mod_tab_tooltip");
    public static final String FADE_ENABLED = "settings.chunksfadein.fade_enabled";
    public static final String FADE_TYPE = "settings.chunksfadein.fade_type";
    public static final String FADE_TIME = "settings.chunksfadein.fade_time";
    public static final String FADE_NEAR_PLAYER = "settings.chunksfadein.fade_near_player";
    public static final String ANIMATION_ENABLED = "settings.chunksfadein.animation_enabled";
    public static final String ANIMATION_TYPE = "settings.chunksfadein.animation_type";
    public static final String ANIMATION_CURVE = "settings.chunksfadein.animation_curve";
    public static final String ANIMATION_OFFSET = "settings.chunksfadein.animation_start";
    public static final String ANIMATION_ANGLE = "settings.chunksfadein.animation_angle";
    public static final String ANIMATION_FACTOR = "settings.chunksfadein.animation_factor";
    public static final String ANIMATE_NEAR_PLAYER = "settings.chunksfadein.animate_near_player";
    public static final String ANIMATION_TIME = "settings.chunksfadein.animation_time";
    public static final String CURVATURE_ENABLED = "settings.chunksfadein.world_curvature_enabled";
    public static final String CURVATURE = "settings.chunksfadein.world_curvature";
    public static final MutableComponent IRIS_WARNING = Component.translatable("settings.chunksfadein.iris_warning");

    public static final int[] CURVATURE_VALUES;

    static {
        CURVATURE_VALUES = new int[16];

        for (int i = 0; i < 16; i++) {
            int curvatureValue;
            if (i < 8)
                curvatureValue = (int) -Math.pow(2, 16 - i);
            else
                curvatureValue = (int) Math.pow(2, i + 1);

            CURVATURE_VALUES[i] = curvatureValue;
        }
    }

    private final Screen parent;
    private boolean dirty = false;
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
        context.drawCenteredString(font, title, width / 2, 12, 16777215 | 255 << 24);
    }

    @Override
    public void onClose() {
        Config.save();

        if (dirty)
            ShaderUtils.reloadWorldRenderer();

        minecraft.setScreen(parent);
    }

    private void rebuildList() {
        if (list != null)
            removeWidget(list);

        addRenderableWidget(list = buildList());
    }

    private CFIListWidget buildList() {
        Runnable markDirty = () -> dirty = true;

        CFIListWidget list = new CFIListWidget(minecraft, this, width, height - 64, 28);

        CFIButton modEnabled = CFIButtonBuilder.choice(MOD_ENABLED, Config.MOD_ENABLED_KEY)
            .onPress(markDirty)
            .build();
        CFIButton updateNotifier = CFIButtonBuilder.toggle(UPDATE_NOTIFIER_ENABLED, Config.UPDATE_NOTIFIER_ENABLED_KEY)
            .build();
        list.add(modEnabled, updateNotifier);

        CFIButton buttonInSettings = CFIButtonBuilder.toggle(MOD_TAB_ENABLED, Config.SHOW_MOD_TAB_IN_SETTINGS_KEY)
            .build();
        list.add(buttonInSettings);

        CFIButton fadeEnabled = CFIButtonBuilder.choice(FADE_ENABLED, Config.FADE_ENABLED_KEY)
            .onPress(markDirty)
            .applyIf(CompatibilityHook.isIrisShaderPackInUse(), b -> b.tooltip(IRIS_WARNING))
            .build();
        CFIButton fadeType = CFIButtonBuilder.cycle(FADE_TYPE, Config.FADE_TYPE_KEY, FadeType.class)
            .onPress(markDirty)
            .build();
        CFISlider fadeTime = new CFISliderBuilder()
            .getValue(() -> Config.secondsFromFadeChange() / Config.MAX_FADE_TIME)
            .applyValue(v -> Config.setDouble(Config.FADE_TIME_KEY, v * Config.MAX_FADE_TIME))
            .displayText(v -> GuiUtils.text(FADE_TIME, String.valueOf(MathUtils.round(Config.secondsFromFadeChange(), 2))).append(UNITS_SECONDS))
            .tooltip(GuiUtils.tooltip(FADE_TIME))
            .build();
        CFIButton fadeNearPlayer = CFIButtonBuilder.choice(FADE_NEAR_PLAYER, Config.FADE_NEAR_PLAYER_KEY)
            .build();
        list.add(fadeEnabled);
        list.add(fadeType, fadeTime, fadeTime.makeResetButton(Config.FADE_TIME_KEY));
        list.add(fadeNearPlayer);

        CFIButton animationEnabled = CFIButtonBuilder.choice(ANIMATION_ENABLED, Config.ANIMATION_ENABLED_KEY)
            .onPress(markDirty)
            .build();
        CFIButton animationCurve = CFIButtonBuilder.cycle(ANIMATION_CURVE, Config.ANIMATION_CURVE_KEY, Curve.class)
            .build();
        CFIButton animationType = CFIButtonBuilder.cycle(ANIMATION_TYPE, Config.ANIMATION_TYPE_KEY, AnimationType.class)
            .onPress(markDirty)
            .build();
        CFISlider animationOffset = CFISliderBuilder.range(ANIMATION_OFFSET, Config.ANIMATION_OFFSET_KEY, UNITS_BLOCKS)
            .build();
        CFISlider animationAngle = CFISliderBuilder.range(ANIMATION_ANGLE, Config.ANIMATION_ANGLE_KEY, UNITS_DEGREES)
            .build();
        CFISlider animationFactor = CFISliderBuilder.range(ANIMATION_FACTOR, Config.ANIMATION_FACTOR_KEY, 2)
            .build();
        CFISlider animationTime = new CFISliderBuilder()
            .getValue(() -> Config.secondsFromAnimationChange() / Config.MAX_ANIMATION_TIME)
            .applyValue(v -> Config.setDouble(Config.ANIMATION_TIME_KEY, v * Config.MAX_ANIMATION_TIME))
            .displayText(v -> GuiUtils.text(ANIMATION_TIME, String.valueOf(MathUtils.round(Config.secondsFromAnimationChange(), 2))).append(UNITS_SECONDS))
            .tooltip(GuiUtils.tooltip(ANIMATION_TIME))
            .build();
        CFIButton animateNearPlayer = CFIButtonBuilder.choice(ANIMATE_NEAR_PLAYER, Config.ANIMATE_NEAR_PLAYER_KEY)
            .build();
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

        CFIButton curvatureEnabled = CFIButtonBuilder.choice(CURVATURE_ENABLED, Config.CURVATURE_ENABLED_KEY)
            .onPress(markDirty)
            .applyIf(CompatibilityHook.isIrisShaderPackInUse(), b -> b.tooltip(IRIS_WARNING))
            .build();
        CFISlider curvatureFactor = new CFISliderBuilder()
            .getValue(() -> curvatureValueIdx(Config.worldCurvature) / 16D)
            .applyValue(v -> {
                Config.setInteger(Config.CURVATURE_KEY, CURVATURE_VALUES[MathUtils.roundToInt(v * 15)]);

                markDirty.run();
            })
            .displayText(v -> GuiUtils.text(CURVATURE, String.valueOf(Config.worldCurvature)))
            .tooltip(GuiUtils.tooltip(CURVATURE))
            .build();
        list.add(curvatureEnabled, curvatureFactor, curvatureFactor.makeResetButton(Config.CURVATURE_KEY));

        return list;
    }

    public static int curvatureValueIdx(int value) {
        for (int i = 0; i < CURVATURE_VALUES.length; i++)
            if (Config.worldCurvature == CURVATURE_VALUES[i])
                return i;

        return 0;
    }
}
