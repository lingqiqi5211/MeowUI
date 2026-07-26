package io.github.lingqiqi5211.meowui.theme

/**
 * 从种子色派生 Material 3 Expressive 配色方案时使用的调色板风格。
 *
 * 仅影响 Material 3 Expressive 分支的配色生成；Miuix 分支始终保持系统 Monet
 * 或 Miuix 自身的 Light/Dark 配色。
 */
enum class MeowPaletteStyle {
    /** Material 3 默认风格，与系统动态取色观感最接近。 */
    TonalSpot,

    /** 低饱和度中性风格。 */
    Neutral,

    /** 高饱和度鲜艳风格。 */
    Vibrant,

    /** Material 3 Expressive 强调风格。 */
    Expressive,

    /** 按色轮旋转的多彩风格。 */
    Rainbow,

    /** 高对比撞色风格。 */
    FruitSalad,

    /** 黑白灰单色风格。 */
    Monochrome,

    /** 忠实还原种子色的风格。 */
    Fidelity,

    /** 面向内容取色（如封面图）的风格。 */
    Content,
}
