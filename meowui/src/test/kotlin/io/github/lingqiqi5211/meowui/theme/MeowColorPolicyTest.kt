package io.github.lingqiqi5211.meowui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeowColorPolicyTest {
    @Test
    fun everyPaletteAndSpecGeneratesCompleteLightAndDarkSchemes() {
        for (palette in MeowPaletteStyle.entries) {
            for (spec in listOf(null) + MeowColorSpec.entries) {
                val settings = effectiveColorSettings(palette, spec)
                assertTrue(settings.palette in availablePaletteStyles)
                assertTrue(settings.spec in availableColorSpecs(settings.palette))
                for (dark in listOf(false, true)) {
                    val scheme = meowMaterialColorScheme(Color(0xFF6750A4), dark, palette, spec)
                    assertEquals(1f, scheme.primary.alpha)
                    assertEquals(1f, scheme.surfaceContainerHighest.alpha)
                    assertFalse(scheme.primary == scheme.onPrimary)
                    // 同一有效设置必须产生相同颜色，也覆盖持久化的旧/不可用组合。
                    val normalized = meowMaterialColorScheme(Color(0xFF6750A4), dark, settings.palette, settings.spec)
                    assertEquals(scheme.primary, normalized.primary)
                    assertEquals(scheme.surfaceContainerHighest, normalized.surfaceContainerHighest)
                    assertEquals(scheme.tertiaryFixed, normalized.tertiaryFixed)
                    assertEquals(settings.palette.name, settings.miuixPalette().name)
                    assertEquals(settings.spec.name, settings.miuixSpec().name)
                }
            }
        }
    }

    @Test
    fun modernSpecsAndCmfFollowTheSelectedDependencyPath() {
        assertEquals(MeowColorSpec.Spec2021, effectiveColorSettings(MeowPaletteStyle.Fidelity, MeowColorSpec.Spec2026).spec)
        assertEquals(MeowColorSpec.Spec2025, effectiveColorSettings(MeowPaletteStyle.TonalSpot, null).spec)
        for (palette in MeowPaletteStyle.entries.filter { it.supportsSpec2025 }) {
            assertEquals(MeowColorSpec.Spec2025, effectiveColorSettings(palette, MeowColorSpec.Spec2026).spec)
            assertEquals(listOf(MeowColorSpec.Spec2021, MeowColorSpec.Spec2025), availableColorSpecs(palette))
        }
        if (supportsColorSpec2026) {
            assertEquals(
                MeowColorSettings(MeowPaletteStyle.Cmf, MeowColorSpec.Spec2026),
                effectiveColorSettings(MeowPaletteStyle.Cmf, MeowColorSpec.Spec2021),
            )
            assertEquals(
                "Spec2026",
                effectiveColorSettings(MeowPaletteStyle.Cmf, MeowColorSpec.Spec2026).miuixSpec().name,
            )
        } else {
            assertEquals(
                MeowColorSettings(MeowPaletteStyle.TonalSpot, MeowColorSpec.Spec2025),
                effectiveColorSettings(MeowPaletteStyle.Cmf, MeowColorSpec.Spec2026),
            )
            assertFalse(MeowPaletteStyle.Cmf in availablePaletteStyles)
            assertFalse(MeowColorSpec.Spec2026 in availableColorSpecs(MeowPaletteStyle.TonalSpot))
        }
    }

    @Test
    fun amoledKeepsElevatedSurfaceRoles() {
        val seed = Color(0xFF6750A4)
        for (palette in availablePaletteStyles) {
            val base = meowMaterialColorScheme(seed, true, palette)
            val black = meowMaterialColorScheme(seed, true, palette, isAmoled = true)
            assertEquals(Color.Black, black.surfaceContainer)
            assertEquals(base.surfaceContainerHighest, black.surfaceContainerHighest)
            assertEquals(base.surfaceBright, black.surfaceBright)
        }
    }
}
