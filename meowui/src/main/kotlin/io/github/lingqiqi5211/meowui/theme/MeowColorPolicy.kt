package io.github.lingqiqi5211.meowui.theme

/** 生成器、主题和预览共用同一有效设置，避免回退行为与缓存键不一致。 */
internal data class MeowColorSettings(
    val palette: MeowPaletteStyle,
    val spec: MeowColorSpec,
)

internal fun effectiveColorSettings(
    palette: MeowPaletteStyle,
    spec: MeowColorSpec?,
): MeowColorSettings {
    if (palette == MeowPaletteStyle.Cmf) {
        return if (supportsColorSpec2026) {
            MeowColorSettings(palette, MeowColorSpec.Spec2026)
        } else {
            MeowColorSettings(MeowPaletteStyle.TonalSpot, MeowColorSpec.Spec2025)
        }
    }
    val effectiveSpec =
        when {
            !palette.supportsSpec2025 -> MeowColorSpec.Spec2021

            // utilities 6 的普通调色板会把 2026 降为 2025；仅 CMF 真正使用 2026。
            spec == MeowColorSpec.Spec2026 -> MeowColorSpec.Spec2025

            else -> spec ?: MeowColorSpec.Spec2025
        }
    return MeowColorSettings(palette, effectiveSpec)
}

internal val availablePaletteStyles: List<MeowPaletteStyle>
    get() = MeowPaletteStyle.entries.filter { supportsColorSpec2026 || it != MeowPaletteStyle.Cmf }

internal fun availableColorSpecs(palette: MeowPaletteStyle): List<MeowColorSpec> =
    when {
        palette == MeowPaletteStyle.Cmf && supportsColorSpec2026 -> listOf(MeowColorSpec.Spec2026)
        !palette.supportsSpec2025 -> listOf(MeowColorSpec.Spec2021)
        else -> listOf(MeowColorSpec.Spec2021, MeowColorSpec.Spec2025)
    }
