package io.github.lingqiqi5211.meowui.component

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowAppearanceDefaults
import io.github.lingqiqi5211.meowui.theme.MeowColorSpec
import io.github.lingqiqi5211.meowui.theme.MeowPaletteStyle
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowThemeMode
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.supportsSpec2025
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Card as MiuixCard

/** 可替换的外观设置页文字。 */
@Immutable
data class MeowAppearanceLabels(
    val title: String = "Appearance",
    val themeColor: String = "Theme color",
    val themeMode: String = "Theme mode",
    val systemMode: String = "System",
    val lightMode: String = "Light",
    val darkMode: String = "Dark",
    val amoledDark: String = "AMOLED dark",
    val amoledDarkSummary: String = "Pure black backgrounds when dark theme is active",
    val colorSettings: String = "Colors",
    val paletteStyle: String = "Color style",
    val colorSpec: String = "Color standard",
    val miuixMonet: String = "Monet colors",
    val miuixMonetSummary: String = "Generate colors from wallpaper or a custom seed color",
    val interfaceSettings: String = "Interface",
    val interfaceStyle: String = "Interface style",
    val predictiveBack: String = "Predictive back gesture",
    val predictiveBackSummary: String = "Preview the destination while swiping back",
    val interfaceScale: String = "Interface scale",
    val interfaceScaleSummary: String = "Adjust the size of the entire interface",
)

/**
 * 統一的外觀設定頁。
 *
 * 頁面本身不保存正式設定；每次變更都透過 [onAppearanceChange] 回傳。
 *
 * 頭圖（色票上方的主題預覽）默認使用內置的 [MeowAppearancePreview]，
 * 會按手機/摺疊屏/平板選擇不同的迷你界面形態；[showPreview] 設為 false 可去掉頭圖，
 * [previewContent] 不為 null 時用自定義內容替換內置頭圖。
 */
@Composable
fun MeowAppearancePage(
    appearance: MeowAppearance,
    onAppearanceChange: (MeowAppearance) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    navigationModifier: Modifier = Modifier,
    labels: MeowAppearanceLabels = MeowAppearanceLabels(),
    showPreview: Boolean = true,
    previewContent: (@Composable (MeowAppearance) -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    MeowPreferencePage(
        title = labels.title,
        modifier = modifier,
        onBackClick = onBackClick,
        navigationModifier = navigationModifier,
    ) {
        MeowAppearanceContent(
            appearance = appearance,
            onAppearanceChange = onAppearanceChange,
            labels = labels,
            showPreview = showPreview,
            previewContent = previewContent,
            extraContent = extraContent,
        )
    }
}

/**
 * 外觀設定的內容本體，不含頁面外殼。
 *
 * 供已經自己持有頂欄的宿主使用：把外觀設定放進宿主已有的頁面裡，而不是再套一層帶頂欄
 * 的 [MeowPreferencePage]。參數含義與 [MeowAppearancePage] 相同。
 *
 * 需要放在能提供滾動容器的地方，例如 [MeowPreferenceScreen] 的內容槽位。
 */
@Composable
fun ColumnScope.MeowAppearanceContent(
    appearance: MeowAppearance,
    onAppearanceChange: (MeowAppearance) -> Unit,
    labels: MeowAppearanceLabels = MeowAppearanceLabels(),
    showPreview: Boolean = true,
    previewContent: (@Composable (MeowAppearance) -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    val normalizedScale = appearance.interfaceScale
        .takeIf(Float::isFinite)
        ?.coerceIn(
            MeowAppearanceDefaults.MinInterfaceScale,
            MeowAppearanceDefaults.MaxInterfaceScale,
        ) ?: 1f
    var draftScale by remember(normalizedScale) {
        mutableFloatStateOf(normalizedScale)
    }

    // Miuix 关闭 Monet 后使用原生配色，种子色与调色板设置不再生效，相关选项一并隐藏。
    val colorCustomizable = appearance.style != MeowUiStyle.Miuix || appearance.miuixMonetEnabled

    if (showPreview) {
        previewContent?.invoke(appearance) ?: MeowAppearancePreview()
    }

    AnimatedVisibility(
        visible = colorCustomizable,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        AppearanceColorCard(title = labels.themeColor) {
            MeowColorPicker(
                dynamicColor = appearance.dynamicColor,
                seedColor = appearance.seedColor,
                paletteStyle = appearance.paletteStyle,
                colorSpec = appearance.colorSpec,
                onDynamicColorChange = { enabled ->
                    onAppearanceChange(appearance.copy(dynamicColor = enabled))
                },
                onSeedColorChange = { color ->
                    onAppearanceChange(
                        appearance.copy(
                            dynamicColor = false,
                            seedColor = color,
                        ),
                    )
                },
            )
        }
    }

    Column {
        AppearanceSectionTitle(labels.themeMode)
        val modes = MeowThemeMode.entries
        MeowTabRow(
            tabs = listOf(labels.systemMode, labels.lightMode, labels.darkMode),
            selectedIndex = modes.indexOf(appearance.themeMode),
            onTabSelected = { index ->
                onAppearanceChange(appearance.copy(themeMode = modes[index]))
            },
            modifier = Modifier.fillMaxWidth(),
            style = MeowTabRowStyle.Contour,
        )
    }

    MeowPreferenceSection(title = labels.colorSettings) {
        if (appearance.style == MeowUiStyle.Miuix) {
            MeowSwitchPreference(
                title = labels.miuixMonet,
                summary = labels.miuixMonetSummary,
                checked = appearance.miuixMonetEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(miuixMonetEnabled = enabled))
                },
            )
        }
        // AMOLED 纯黑深色仅 Material 分支生效,作为深色模式的叠加开关。
        if (appearance.style == MeowUiStyle.MaterialExpressive) {
            MeowSwitchPreference(
                title = labels.amoledDark,
                summary = labels.amoledDarkSummary,
                checked = appearance.amoledDarkEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(amoledDarkEnabled = enabled))
                },
            )
        }
        if (colorCustomizable) {
            MeowPopupPreference(
                title = labels.paletteStyle,
                value = appearance.paletteStyle,
                options = MeowPaletteStyle.entries,
                optionLabel = MeowPaletteStyle::displayName,
                onValueChange = { style ->
                    onAppearanceChange(
                        appearance.copy(
                            paletteStyle = style,
                            colorSpec = if (style.supportsSpec2025) {
                                appearance.colorSpec
                            } else {
                                MeowColorSpec.Spec2021
                            },
                        ),
                    )
                },
            )
            val availableColorSpecs = if (appearance.paletteStyle.supportsSpec2025) {
                MeowColorSpec.entries
            } else {
                listOf(MeowColorSpec.Spec2021)
            }
            MeowPopupPreference(
                title = labels.colorSpec,
                value = appearance.colorSpec.takeIf(availableColorSpecs::contains)
                    ?: MeowColorSpec.Spec2021,
                options = availableColorSpecs,
                optionLabel = { spec ->
                    when (spec) {
                        MeowColorSpec.Spec2021 -> "2021"
                        MeowColorSpec.Spec2025 -> "2025"
                    }
                },
                onValueChange = { spec ->
                    onAppearanceChange(appearance.copy(colorSpec = spec))
                },
            )
        }
    }

    MeowPreferenceSection(title = labels.interfaceSettings) {
        MeowPopupPreference(
            title = labels.interfaceStyle,
            value = appearance.style,
            options = MeowUiStyle.entries,
            optionLabel = { style ->
                when (style) {
                    MeowUiStyle.MaterialExpressive -> "Material 3 Expressive"
                    MeowUiStyle.Miuix -> "Miuix"
                }
            },
            onValueChange = { style ->
                onAppearanceChange(appearance.copy(style = style))
            },
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            MeowSwitchPreference(
                title = labels.predictiveBack,
                summary = labels.predictiveBackSummary,
                checked = appearance.predictiveBackEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(predictiveBackEnabled = enabled))
                },
            )
        }
        MeowSliderPreference(
            title = labels.interfaceScale,
            summary = labels.interfaceScaleSummary,
            value = draftScale,
            // 拖动期间只更新本地草稿并量化到 1%，不显示档位点；松手后才提交生效。
            onValueChange = { draftScale = (it * 100).roundToInt() / 100f },
            onValueChangeFinished = {
                onAppearanceChange(appearance.copy(interfaceScale = draftScale))
            },
            valueRange = MeowAppearanceDefaults.MinInterfaceScale..MeowAppearanceDefaults.MaxInterfaceScale,
            valueText = { "${(it * 100).roundToInt()}%" },
        )
    }

    extraContent()
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun AppearanceColorCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppearanceSectionTitle(title)
        Spacer(Modifier.height(8.dp))
        MeowStyleContent(
            materialExpressive = {
                MaterialSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceBright,
                ) {
                    AppearanceColorCardContent(content)
                }
            },
            miuix = {
                MiuixCard(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp),
                ) {
                    AppearanceColorCardContent(content)
                }
            },
        )
    }
}

@Composable
private fun AppearanceColorCardContent(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        content()
    }
}

@Composable
private fun AppearanceSectionTitle(text: String) {
    BasicText(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        style = MeowTheme.typography.sectionTitle.copy(color = MeowTheme.colors.primary),
    )
}

private fun MeowPaletteStyle.displayName(): String = when (this) {
    MeowPaletteStyle.TonalSpot -> "Tonal spot"
    MeowPaletteStyle.Neutral -> "Neutral"
    MeowPaletteStyle.Vibrant -> "Vibrant"
    MeowPaletteStyle.Expressive -> "Expressive"
    MeowPaletteStyle.Rainbow -> "Rainbow"
    MeowPaletteStyle.FruitSalad -> "Fruit salad"
    MeowPaletteStyle.Monochrome -> "Monochrome"
    MeowPaletteStyle.Fidelity -> "Fidelity"
    MeowPaletteStyle.Content -> "Content"
}
