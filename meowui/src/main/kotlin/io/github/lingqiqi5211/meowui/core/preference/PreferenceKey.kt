package io.github.lingqiqi5211.meowui.core.preference

class PreferenceKey<T : Any> private constructor(
    val name: String,
    val type: PreferenceType<T>,
    defaultValue: T,
) {
    init {
        require(name.isNotBlank()) { "Preference key name must not be blank." }
    }

    private val storedDefaultValue = type.copyValue(defaultValue)

    val defaultValue: T
        get() = type.copyValue(storedDefaultValue)

    override fun equals(other: Any?): Boolean =
        other is PreferenceKey<*> && name == other.name && type == other.type

    override fun hashCode(): Int = 31 * name.hashCode() + type.hashCode()

    override fun toString(): String = "PreferenceKey(name=$name, type=${type.valueName})"

    companion object {
        operator fun invoke(
            name: String,
            defaultValue: Boolean,
        ): PreferenceKey<Boolean> = boolean(name, defaultValue)

        operator fun invoke(
            name: String,
            defaultValue: Int,
        ): PreferenceKey<Int> = int(name, defaultValue)

        operator fun invoke(
            name: String,
            defaultValue: Long,
        ): PreferenceKey<Long> = long(name, defaultValue)

        operator fun invoke(
            name: String,
            defaultValue: Float,
        ): PreferenceKey<Float> = float(name, defaultValue)

        operator fun invoke(
            name: String,
            defaultValue: String,
        ): PreferenceKey<String> = string(name, defaultValue)

        operator fun invoke(
            name: String,
            defaultValue: Set<String>,
        ): PreferenceKey<Set<String>> = stringSet(name, defaultValue)

        operator fun <T : Any> invoke(
            name: String,
            defaultValue: T,
        ): PreferenceKey<T> = throw IllegalArgumentException(
            if (defaultValue is Set<*>) {
                "Preference '$name' only supports Set<String> defaults."
            } else {
                "Preference '$name' does not support ${defaultValue::class.simpleName} defaults."
            },
        )

        fun boolean(
            name: String,
            defaultValue: Boolean = false,
        ): PreferenceKey<Boolean> = PreferenceKey(name, PreferenceType.Boolean, defaultValue)

        fun int(
            name: String,
            defaultValue: Int = 0,
        ): PreferenceKey<Int> = PreferenceKey(name, PreferenceType.Int, defaultValue)

        fun long(
            name: String,
            defaultValue: Long = 0L,
        ): PreferenceKey<Long> = PreferenceKey(name, PreferenceType.Long, defaultValue)

        fun float(
            name: String,
            defaultValue: Float = 0f,
        ): PreferenceKey<Float> = PreferenceKey(name, PreferenceType.Float, defaultValue)

        fun string(
            name: String,
            defaultValue: String = "",
        ): PreferenceKey<String> = PreferenceKey(name, PreferenceType.String, defaultValue)

        fun stringSet(
            name: String,
            defaultValue: Set<String> = emptySet(),
        ): PreferenceKey<Set<String>> = PreferenceKey(name, PreferenceType.StringSet, defaultValue)
    }
}

class PreferenceTypeMismatchException(
    val keyName: String,
    val expectedType: PreferenceType<*>,
    val actualType: PreferenceType<*>,
) : IllegalStateException(
    "Preference '$keyName' is ${actualType.valueName}, not ${expectedType.valueName}.",
)
