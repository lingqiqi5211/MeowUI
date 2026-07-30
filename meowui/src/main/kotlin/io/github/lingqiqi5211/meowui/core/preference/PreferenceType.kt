package io.github.lingqiqi5211.meowui.core.preference

sealed class PreferenceType<T : Any> private constructor(
    val valueName: kotlin.String,
) {
    data object Boolean : PreferenceType<kotlin.Boolean>("Boolean")

    data object Int : PreferenceType<kotlin.Int>("Int")

    data object Long : PreferenceType<kotlin.Long>("Long")

    data object Float : PreferenceType<kotlin.Float>("Float")

    data object String : PreferenceType<kotlin.String>("String")

    data object StringSet : PreferenceType<Set<kotlin.String>>("StringSet")
}

internal fun PreferenceType<*>.accepts(value: Any): kotlin.Boolean = when (this) {
    PreferenceType.Boolean -> value is kotlin.Boolean
    PreferenceType.Int -> value is kotlin.Int
    PreferenceType.Long -> value is kotlin.Long
    PreferenceType.Float -> value is kotlin.Float
    PreferenceType.String -> value is kotlin.String
    PreferenceType.StringSet -> value is Set<*> && value.all { it is kotlin.String }
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> PreferenceType<T>.copyValue(value: Any): T {
    require(accepts(value)) {
        "Expected $valueName but received ${value::class.simpleName}."
    }

    val copy = when (this) {
        PreferenceType.StringSet -> (value as Set<*>).mapTo(linkedSetOf()) { it as kotlin.String }
        else -> value
    }
    return copy as T
}
