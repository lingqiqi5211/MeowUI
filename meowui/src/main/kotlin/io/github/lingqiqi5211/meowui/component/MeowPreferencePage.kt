package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A complete settings page with a style-aware top bar and scrollable preference content.
 *
 * Preference components inside [content] can bind directly to a PreferenceKey supplied by the
 * nearest preference provider.
 */
@Composable
fun MeowPreferencePage(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onBackClick: (() -> Unit)? = null,
    /**
     * Applied to the back button built from [onBackClick].
     *
     * Ignored when [navigationIcon] is supplied, since that content brings its own
     * modifier.
     */
    navigationModifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionItems: List<MeowTopBarAction> = emptyList(),
    bottomBar: @Composable () -> Unit = {},
    snackbarState: MeowSnackbarState? = null,
    effect: MeowScaffoldEffect = MeowScaffoldEffect(),
    content: @Composable ColumnScope.() -> Unit,
) {
    MeowScaffold(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        onBackClick = onBackClick,
        navigationModifier = navigationModifier,
        navigationIcon = navigationIcon,
        actionItems = actionItems,
        bottomBar = bottomBar,
        snackbarState = snackbarState,
        effect = effect,
    ) { _ ->
        MeowPreferenceScreen(content = content)
    }
}
