package io.github.lingqiqi5211.meowui.theme

import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported

/** 背景模糊的可用性，库内所有模糊分支与外观页的模糊开关共用这一个判断。 */
object MeowBlur {
    /**
     * 设备能否渲染背景模糊。
     *
     * miuix 的模糊经 RuntimeShader 实现，Android 13 起可用；更低版本不显示模糊开关，
     * 也不启用模糊，`MeowAppearance.blurEnabled` 的值被忽略。
     */
    val isSupported: Boolean
        get() = isRuntimeShaderSupported()
}
