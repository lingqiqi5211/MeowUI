package io.github.lingqiqi5211.meowui.theme

/**
 * 从种子色派生配色方案时使用的调色板风格。
 *
 * 两个分支共用：Material 3 Expressive 由 tonal palette 展开，
 * Miuix 由 Miuix Monet 引擎生成同风格配色。
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
