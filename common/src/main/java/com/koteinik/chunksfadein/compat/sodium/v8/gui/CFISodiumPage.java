package com.koteinik.chunksfadein.compat.sodium.v8.gui;

import com.koteinik.chunksfadein.core.*;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.koteinik.chunksfadein.platform.Services;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

import static com.koteinik.chunksfadein.MathUtils.roundToInt;
import static com.koteinik.chunksfadein.config.Config.*;
import static com.koteinik.chunksfadein.crowdin.Translations.translatable;
import static com.koteinik.chunksfadein.gui.GuiUtils.tooltip;
import static com.koteinik.chunksfadein.gui.SettingsScreen.*;
import static net.minecraft.network.chat.Component.empty;

public class CFISodiumPage implements ConfigEntryPoint {
	private static final String SODIUM_PAGE_NAME = "settings.chunksfadein.sodium_page_name";
	private final CFIOptionsStorage cfiStorage = new CFIOptionsStorage();
	private final StorageEventHandler handler = cfiStorage::flush;

	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		boolean forceEnableTab = !Services.PLATFORM.isForge() && !CompatibilityHook.isModMenuLoaded;
		if (!forceEnableTab && !showModTabInSettings)
			return;

		builder.registerOwnModOptions()
			.setIcon(ResourceLocation.parse("chunksfadein:icon.png"))
			.addPage(builder.createOptionPage()
				.setName(translatable(SODIUM_PAGE_NAME))
				.addOptionGroup(builder.createOptionGroup()
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:mod_enabled"))
							.setStorageHandler(handler)
							.setName(translatable(MOD_ENABLED))
							.setTooltip(tooltip(MOD_ENABLED))
							.setBinding(v -> cfiStorage.setBooleanDirty(MOD_ENABLED_KEY, v), () -> isModEnabled)
							.setDefaultValue(DEFAULT_MOD_ENABLED)
							.setImpact(OptionImpact.VARIES)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:update_notifier_enabled"))
							.setStorageHandler(handler)
							.setName(translatable(UPDATE_NOTIFIER_ENABLED))
							.setTooltip(tooltip(UPDATE_NOTIFIER_ENABLED))
							.setBinding(v -> setBoolean(UPDATE_NOTIFIER_ENABLED_KEY, v), () -> isUpdateNotifierEnabled)
							.setDefaultValue(DEFAULT_UPDATE_NOTIFIER_ENABLED)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:mod_tab_enabled"))
							.setStorageHandler(handler)
							.setName(translatable(MOD_TAB_ENABLED))
							.setTooltip(tooltip(MOD_TAB_ENABLED).append(forceEnableTab
								? Component.literal("\n").append(MOD_TAB_TOOLTIP)
								: empty()))
							.setEnabled(!forceEnableTab)
							.setBinding(
								v -> setBoolean(SHOW_MOD_TAB_IN_SETTINGS_KEY, v),
								() -> forceEnableTab || showModTabInSettings
							)
							.setDefaultValue(DEFAULT_SHOW_MOD_TAB_IN_SETTINGS)
					)
				)
				.addOptionGroup(builder.createOptionGroup()
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:fade_enabled"))
							.setStorageHandler(handler)
							.setName(translatable(FADE_ENABLED))
							.setTooltip(tooltip(FADE_ENABLED))
							.setBinding(v -> cfiStorage.setBooleanDirty(FADE_ENABLED_KEY, v), () -> isFadeEnabled)
							.setDefaultValue(DEFAULT_FADE_ENABLED)
							.setImpact(OptionImpact.LOW)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:fade_patch_shaders"))
							.setStorageHandler(handler)
							.setName(translatable(FADE_PATCH_SHADERS))
							.setTooltip(tooltip(FADE_PATCH_SHADERS))
							.setBinding(
								v -> cfiStorage.setBooleanDirty(FADE_PATCH_SHADERS_KEY, v),
								() -> patchShaderFade
							)
							.setDefaultValue(DEFAULT_FADE_PATCH_SHADERS)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:fade_near_player"))
							.setStorageHandler(handler)
							.setName(translatable(FADE_NEAR_PLAYER))
							.setTooltip(tooltip(FADE_NEAR_PLAYER))
							.setBinding(v -> setBoolean(FADE_NEAR_PLAYER_KEY, v), () -> fadeNearPlayer)
							.setDefaultValue(DEFAULT_FADE_NEAR_PLAYER)
					)
					.addOption(
						builder.createEnumOption(ResourceLocation.parse("chunksfadein:fade_type"), FadeType.class)
							.setStorageHandler(handler)
							.setElementNameProvider(FadeType::getTranslation)
							.setName(translatable(FADE_TYPE))
							.setTooltip(tooltip(FADE_TYPE))
							.setBinding(v -> cfiStorage.setEnumDirty(FADE_TYPE_KEY, v), () -> fadeType)
							.setDefaultValue(DEFAULT_FADE_TYPE)
					)
					.addOption(
						builder.createEnumOption(ResourceLocation.parse("chunksfadein:fade_curve"), FadeCurve.class)
							.setStorageHandler(handler)
							.setElementNameProvider(FadeCurve::getTranslation)
							.setName(translatable(FADE_CURVE))
							.setTooltip(tooltip(FADE_CURVE))
							.setBinding(v -> cfiStorage.setEnumDirty(FADE_CURVE_KEY, v), () -> fadeCurve)
							.setDefaultValue(DEFAULT_FADE_CURVE)
					)
					.addOption(
						builder.createEnumOption(ResourceLocation.parse("chunksfadein:fade_mix_type"), FadeMixType.class)
							.setStorageHandler(handler)
							.setElementNameProvider(FadeMixType::getTranslation)
							.setName(translatable(FADE_MIX_TYPE))
							.setTooltip(tooltip(FADE_MIX_TYPE))
							.setBinding(v -> cfiStorage.setEnumDirty(FADE_MIX_TYPE_KEY, v), () -> fadeMixType)
							.setDefaultValue(DEFAULT_FADE_MIX_TYPE)
					)
					.addOption(
						builder.createEnumOption(ResourceLocation.parse("chunksfadein:fog_override"), FogOverrideMode.class)
							.setStorageHandler(handler)
							.setElementNameProvider(FogOverrideMode::getTranslation)
							.setName(translatable(FOG_OVERRIDE))
							.setTooltip(tooltip(FOG_OVERRIDE))
							.setBinding(v -> cfiStorage.setEnumDirty(FOG_OVERRIDE_KEY, v), () -> fogOverrideMode)
							.setDefaultValue(DEFAULT_FOG_OVERRIDE)
					)
					.addOption(
						builder.createIntegerOption(ResourceLocation.parse("chunksfadein:fade_time"))
							.setStorageHandler(handler)
							.setName(translatable(FADE_TIME))
							.setTooltip(tooltip(FADE_TIME))
							.setRange(roundToInt(MIN_FADE_TIME * 100), roundToInt(MAX_FADE_TIME * 100), 1)
							.setValueFormatter(scaled(100, UNITS_SECONDS))
							.setBinding(
								v -> setDouble(FADE_TIME_KEY, v / 100D),
								() -> roundToInt(secondsFromFadeChange() * 100)
							)
							.setDefaultValue(roundToInt(DEFAULT_FADE_TIME * 100))
					)
				)
				.addOptionGroup(builder.createOptionGroup()
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:animation_enabled"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATION_ENABLED))
							.setTooltip(tooltip(ANIMATION_ENABLED))
							.setBinding(
								v -> cfiStorage.setBooleanDirty(ANIMATION_ENABLED_KEY, v),
								() -> isAnimationEnabled
							)
							.setDefaultValue(DEFAULT_ANIMATION_ENABLED)
							.setImpact(OptionImpact.LOW)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:animation_patch_shaders"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATION_PATCH_SHADERS))
							.setTooltip(tooltip(ANIMATION_PATCH_SHADERS))
							.setBinding(
								v -> cfiStorage.setBooleanDirty(ANIMATION_PATCH_SHADERS_KEY, v),
								() -> patchShaderAnimation
							)
							.setDefaultValue(DEFAULT_ANIMATION_PATCH_SHADERS)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:animate_near_player"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATE_NEAR_PLAYER))
							.setTooltip(tooltip(ANIMATE_NEAR_PLAYER))
							.setBinding(v -> setBoolean(ANIMATE_NEAR_PLAYER_KEY, v), () -> animateNearPlayer)
							.setDefaultValue(DEFAULT_ANIMATE_NEAR_PLAYER)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:animate_with_dh"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATE_WITH_DH))
							.setTooltip(tooltip(ANIMATE_WITH_DH))
							.setBinding(v -> setBoolean(ANIMATE_WITH_DH_KEY, v), () -> animateWithDH)
							.setDefaultValue(DEFAULT_ANIMATE_WITH_DH)
					)
					.addOption(
						builder.createEnumOption(ResourceLocation.parse("chunksfadein:animation_curve"), AnimationCurve.class)
							.setStorageHandler(handler)
							.setElementNameProvider(AnimationCurve::getTranslation)
							.setName(translatable(ANIMATION_CURVE))
							.setTooltip(tooltip(ANIMATION_CURVE))
							.setBinding(v -> cfiStorage.setEnumDirty(ANIMATION_CURVE_KEY, v), () -> animationCurve)
							.setDefaultValue(DEFAULT_ANIMATION_CURVE)
					)
					.addOption(
						builder.createEnumOption(ResourceLocation.parse("chunksfadein:animation_type"), AnimationType.class)
							.setStorageHandler(handler)
							.setElementNameProvider(AnimationType::getTranslation)
							.setName(translatable(ANIMATION_TYPE))
							.setTooltip(tooltip(ANIMATION_TYPE))
							.setBinding(v -> cfiStorage.setEnumDirty(ANIMATION_TYPE_KEY, v), () -> animationType)
							.setDefaultValue(DEFAULT_ANIMATION_TYPE)
					)
					.addOption(
						builder.createIntegerOption(ResourceLocation.parse("chunksfadein:animation_time"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATION_TIME))
							.setTooltip(tooltip(ANIMATION_TIME))
							.setRange(roundToInt(MIN_ANIMATION_TIME * 100), roundToInt(MAX_ANIMATION_TIME * 100), 1)
							.setValueFormatter(scaled(100, UNITS_SECONDS))
							.setBinding(
								v -> setDouble(ANIMATION_TIME_KEY, v / 100D),
								() -> roundToInt(secondsFromAnimationChange() * 100)
							)
							.setDefaultValue(roundToInt(DEFAULT_ANIMATION_TIME * 100))
					)
					.addOption(
						builder.createIntegerOption(ResourceLocation.parse("chunksfadein:animation_offset"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATION_OFFSET))
							.setTooltip(tooltip(ANIMATION_OFFSET))
							.setRange(roundToInt(MIN_ANIMATION_OFFSET * 100), roundToInt(MAX_ANIMATION_OFFSET * 100), 1)
							.setValueFormatter(scaled(100, UNITS_BLOCKS))
							.setBinding(
								v -> setDouble(ANIMATION_OFFSET_KEY, v / 100D),
								() -> roundToInt(animationOffset * 100)
							)
							.setDefaultValue(roundToInt(DEFAULT_ANIMATION_OFFSET * 100))
					)
					.addOption(
						builder.createIntegerOption(ResourceLocation.parse("chunksfadein:animation_angle"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATION_ANGLE))
							.setTooltip(tooltip(ANIMATION_ANGLE))
							.setRange(roundToInt(MIN_ANIMATION_ANGLE), roundToInt(MAX_ANIMATION_ANGLE), 1)
							.setValueFormatter(number(UNITS_DEGREES))
							.setBinding(
								v -> setDouble(ANIMATION_ANGLE_KEY, (double) v),
								() -> roundToInt(animationAngle)
							)
							.setDefaultValue(roundToInt(DEFAULT_ANIMATION_ANGLE))
					)
					.addOption(
						builder.createIntegerOption(ResourceLocation.parse("chunksfadein:animation_factor"))
							.setStorageHandler(handler)
							.setName(translatable(ANIMATION_FACTOR))
							.setTooltip(tooltip(ANIMATION_FACTOR))
							.setRange(roundToInt(MIN_ANIMATION_FACTOR * 100), roundToInt(MAX_ANIMATION_FACTOR * 100), 1)
							.setValueFormatter(scaled(100))
							.setBinding(
								v -> setDouble(ANIMATION_FACTOR_KEY, v / 100D),
								() -> roundToInt(animationFactor * 100)
							)
							.setDefaultValue(roundToInt(DEFAULT_ANIMATION_FACTOR * 100))
					)
				)
				.addOptionGroup(builder.createOptionGroup()
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:curvature_enabled"))
							.setStorageHandler(handler)
							.setName(translatable(CURVATURE_ENABLED))
							.setTooltip(tooltip(CURVATURE_ENABLED))
							.setBinding(
								v -> cfiStorage.setBooleanDirty(CURVATURE_ENABLED_KEY, v),
								() -> isCurvatureEnabled
							)
							.setDefaultValue(DEFAULT_CURVATURE_ENABLED)
							.setImpact(OptionImpact.LOW)
					)
					.addOption(
						builder.createBooleanOption(ResourceLocation.parse("chunksfadein:curvature_patch_shaders"))
							.setStorageHandler(handler)
							.setName(translatable(CURVATURE_PATCH_SHADERS))
							.setTooltip(tooltip(CURVATURE_PATCH_SHADERS))
							.setBinding(
								v -> cfiStorage.setBooleanDirty(CURVATURE_PATCH_SHADERS_KEY, v),
								() -> patchShaderCurvature
							)
							.setDefaultValue(DEFAULT_CURVATURE_PATCH_SHADERS)
					)
					.addOption(
						builder.createIntegerOption(ResourceLocation.parse("chunksfadein:curvature"))
							.setStorageHandler(handler)
							.setName(translatable(CURVATURE))
							.setTooltip(tooltip(CURVATURE))
							.setRange(0, 15, 1)
							.setValueFormatter(v -> Component.literal(String.valueOf(CURVATURE_VALUES[v])))
							.setBinding(
								v -> cfiStorage.setIntegerDirty(CURVATURE_KEY, CURVATURE_VALUES[v]),
								() -> curvatureValueIdx(worldCurvature)
							)
							.setDefaultValue(curvatureValueIdx(DEFAULT_CURVATURE))
					)
				)
			);
	}

	private static ControlValueFormatter scaled(int scale) {
		return scaled(scale, empty());
	}

	private static ControlValueFormatter scaled(int scale, Component units) {
		return v -> Component.literal(String.valueOf((double) v / scale)).append(units);
	}

	private static ControlValueFormatter number(Component units) {
		return v -> Component.literal(String.valueOf(v)).append(units);
	}

	private static <T extends Enum<T> & TranslatableEnum> Component[] translations(Class<T> clazz) {
		return Arrays.stream(clazz.getEnumConstants())
			.map(TranslatableEnum::getTranslation)
			.toArray(Component[]::new);
	}
}
