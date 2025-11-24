package com.koteinik.chunksfadein.compat.embeddium;

import com.google.common.collect.ImmutableList;
import com.koteinik.chunksfadein.core.*;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.koteinik.chunksfadein.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.embeddedt.embeddium.api.options.OptionIdentifier;
import org.embeddedt.embeddium.api.options.control.ControlValueFormatter;
import org.embeddedt.embeddium.api.options.control.CyclingControl;
import org.embeddedt.embeddium.api.options.control.SliderControl;
import org.embeddedt.embeddium.api.options.control.TickBoxControl;
import org.embeddedt.embeddium.api.options.structure.OptionGroup;
import org.embeddedt.embeddium.api.options.structure.OptionImpact;
import org.embeddedt.embeddium.api.options.structure.OptionImpl;
import org.embeddedt.embeddium.api.options.structure.OptionPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.koteinik.chunksfadein.MathUtils.roundToInt;
import static com.koteinik.chunksfadein.config.Config.*;
import static com.koteinik.chunksfadein.gui.GuiUtils.tooltip;
import static com.koteinik.chunksfadein.gui.SettingsScreen.*;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.translatable;

public class CFIEmbeddiumPage extends OptionPage {
	private static final String SODIUM_PAGE_NAME = "settings.chunksfadein.sodium_page_name";
	private static final CFIOptionsStorage cfiStorage = new CFIOptionsStorage();

	public CFIEmbeddiumPage() {
		super(
			OptionIdentifier.create("chunksfadein", "chunksfadein_options"),
			Component.translatable(SODIUM_PAGE_NAME),
			ImmutableList.copyOf(makeOptions())
		);
	}

	private static List<OptionGroup> makeOptions() {
		List<OptionGroup> groups = new ArrayList<>();

		boolean forceEnableTab = !Services.PLATFORM.isForge() && !CompatibilityHook.isModMenuLoaded;
		groups.add(OptionGroup.createBuilder()
			.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "mod"))
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "mod_enabled"))
				.setName(translatable(MOD_ENABLED))
				.setTooltip(tooltip(MOD_ENABLED))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> cfiStorage.setBooleanDirty(MOD_ENABLED_KEY, v), c -> isModEnabled)
				.setImpact(OptionImpact.LOW)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "update_notifier"))
				.setName(translatable(UPDATE_NOTIFIER_ENABLED))
				.setTooltip(tooltip(UPDATE_NOTIFIER_ENABLED))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> setBoolean(UPDATE_NOTIFIER_ENABLED_KEY, v), c -> isUpdateNotifierEnabled)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "mod_tab"))
				.setName(translatable(MOD_TAB_ENABLED))
				.setTooltip(tooltip(MOD_TAB_ENABLED).append(forceEnableTab
					? Component.literal("\n").append(MOD_TAB_TOOLTIP)
					: empty()))
				.setControl(TickBoxControl::new)
				.setEnabled(!forceEnableTab)
				.setBinding(
					(c, v) -> setBoolean(SHOW_MOD_TAB_IN_SETTINGS_KEY, v),
					c -> forceEnableTab || showModTabInSettings
				)
				.build())
			.build());

		groups.add(OptionGroup.createBuilder()
			.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "fade"))
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "fade_enabled"))
				.setName(translatable(FADE_ENABLED))
				.setTooltip(tooltip(FADE_ENABLED))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> cfiStorage.setBooleanDirty(FADE_ENABLED_KEY, v), c -> isFadeEnabled)
				.setImpact(OptionImpact.LOW)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "fade_near_player"))
				.setName(translatable(FADE_NEAR_PLAYER))
				.setTooltip(tooltip(FADE_NEAR_PLAYER))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> setBoolean(FADE_NEAR_PLAYER_KEY, v), c -> fadeNearPlayer)
				.build())
			.add(OptionImpl.createBuilder(FadeType.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "fade_type"))
				.setName(translatable(FADE_TYPE))
				.setTooltip(tooltip(FADE_TYPE))
				.setControl(o -> new CyclingControl<>(o, FadeType.class, translations(FadeType.class)))
				.setBinding((c, v) -> cfiStorage.setIntegerDirty(FADE_TYPE_KEY, v.ordinal()), c -> fadeType)
				.build())
			.add(OptionImpl.createBuilder(FogOverrideMode.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "fog_override"))
				.setName(translatable(FOG_OVERRIDE))
				.setTooltip(tooltip(FOG_OVERRIDE, "tooltip_pre_1_21_6"))
				.setControl(o -> new CyclingControl<>(o, FogOverrideMode.class, translations(FogOverrideMode.class)))
				.setBinding((c, v) -> cfiStorage.setIntegerDirty(FOG_OVERRIDE_KEY, v.ordinal()), c -> fogOverrideMode)
				.build())
			.add(OptionImpl.createBuilder(int.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "fade_time"))
				.setName(translatable(FADE_TIME))
				.setTooltip(tooltip(FADE_TIME))
				.setControl(o -> new SliderControl(
					o,
					roundToInt(MIN_FADE_TIME * 100),
					roundToInt(MAX_FADE_TIME * 100),
					1,
					scaled(100, UNITS_SECONDS)
				))
				.setBinding(
					(c, v) -> setDouble(FADE_TIME_KEY, v / 100D),
					c -> roundToInt(secondsFromFadeChange() * 100)
				)
				.build())
			.build());

		groups.add(OptionGroup.createBuilder()
			.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animation"))
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animation_enabled"))
				.setName(translatable(ANIMATION_ENABLED))
				.setTooltip(tooltip(ANIMATION_ENABLED))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> cfiStorage.setBooleanDirty(ANIMATION_ENABLED_KEY, v), c -> isAnimationEnabled)
				.setImpact(OptionImpact.LOW)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animate_near_player"))
				.setName(translatable(ANIMATE_NEAR_PLAYER))
				.setTooltip(tooltip(ANIMATE_NEAR_PLAYER))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> setBoolean(ANIMATE_NEAR_PLAYER_KEY, v), c -> animateNearPlayer)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animate_with_dh"))
				.setName(translatable(ANIMATE_WITH_DH))
				.setTooltip(tooltip(ANIMATE_WITH_DH))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> setBoolean(ANIMATE_WITH_DH_KEY, v), c -> animateWithDH)
				.build())
			.add(OptionImpl.createBuilder(Curve.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "curve"))
				.setName(translatable(ANIMATION_CURVE))
				.setTooltip(tooltip(ANIMATION_CURVE))
				.setControl(o -> new CyclingControl<>(o, Curve.class, translations(Curve.class)))
				.setBinding((c, v) -> cfiStorage.setIntegerDirty(ANIMATION_CURVE_KEY, v.ordinal()), c -> animationCurve)
				.build())
			.add(OptionImpl.createBuilder(AnimationType.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animation_type"))
				.setName(translatable(ANIMATION_TYPE))
				.setTooltip(tooltip(ANIMATION_TYPE))
				.setControl(o -> new CyclingControl<>(o, AnimationType.class, translations(AnimationType.class)))
				.setBinding((c, v) -> cfiStorage.setIntegerDirty(ANIMATION_TYPE_KEY, v.ordinal()), c -> animationType)
				.build())
			.add(OptionImpl.createBuilder(int.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animation_time"))
				.setName(translatable(ANIMATION_TIME))
				.setTooltip(tooltip(ANIMATION_TIME))
				.setControl(o -> new SliderControl(
					o,
					roundToInt(MIN_ANIMATION_TIME * 100),
					roundToInt(MAX_ANIMATION_TIME * 100),
					1,
					scaled(100, UNITS_SECONDS)
				))
				.setBinding(
					(c, v) -> setDouble(ANIMATION_TIME_KEY, v / 100D),
					c -> roundToInt(secondsFromAnimationChange() * 100)
				)
				.build())
			.add(OptionImpl.createBuilder(int.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animation_start"))
				.setName(translatable(ANIMATION_OFFSET))
				.setTooltip(tooltip(ANIMATION_OFFSET))
				.setControl(o -> new SliderControl(
					o,
					roundToInt(MIN_ANIMATION_OFFSET * 100),
					roundToInt(MAX_ANIMATION_OFFSET * 100),
					1,
					scaled(100, UNITS_BLOCKS)
				))
				.setBinding((c, v) -> setDouble(ANIMATION_OFFSET_KEY, v / 100D), c -> roundToInt(animationOffset * 100))
				.build())
			.add(OptionImpl.createBuilder(int.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animation_angle"))
				.setName(translatable(ANIMATION_ANGLE))
				.setTooltip(tooltip(ANIMATION_ANGLE))
				.setControl(o -> new SliderControl(
					o,
					roundToInt(MIN_ANIMATION_ANGLE),
					roundToInt(MAX_ANIMATION_ANGLE),
					1,
					number(UNITS_DEGREES)
				))
				.setBinding((c, v) -> setDouble(ANIMATION_ANGLE_KEY, (double) v), c -> roundToInt(animationAngle))
				.build())
			.add(OptionImpl.createBuilder(int.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "animation_factor"))
				.setName(translatable(ANIMATION_FACTOR))
				.setTooltip(tooltip(ANIMATION_FACTOR))
				.setControl(o -> new SliderControl(
					o,
					roundToInt(MIN_ANIMATION_FACTOR * 100),
					roundToInt(MAX_ANIMATION_FACTOR * 100),
					1,
					scaled(100)
				))
				.setBinding((c, v) -> setDouble(ANIMATION_FACTOR_KEY, v / 100D), c -> roundToInt(animationFactor * 100))
				.build())
			.build());

		groups.add(OptionGroup.createBuilder()
			.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "curvature"))
			.add(OptionImpl.createBuilder(boolean.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "curvature_enabled"))
				.setName(translatable(CURVATURE_ENABLED))
				.setTooltip(tooltip(CURVATURE_ENABLED))
				.setControl(TickBoxControl::new)
				.setBinding((c, v) -> cfiStorage.setBooleanDirty(CURVATURE_ENABLED_KEY, v), c -> isCurvatureEnabled)
				.setImpact(OptionImpact.LOW)
				.build())
			.add(OptionImpl.createBuilder(int.class, cfiStorage)
				.setId(ResourceLocation.fromNamespaceAndPath("chunksfadein", "curvature_value"))
				.setName(translatable(CURVATURE))
				.setTooltip(tooltip(CURVATURE))
				.setControl(o -> new SliderControl(
					o,
					0,
					15,
					1,
					v -> Component.literal(String.valueOf(CURVATURE_VALUES[v]))
				))
				.setBinding(
					(c, v) -> cfiStorage.setIntegerDirty(CURVATURE_KEY, CURVATURE_VALUES[v]),
					c -> curvatureValueIdx(worldCurvature)
				)
				.build())
			.build());

		return groups;
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
