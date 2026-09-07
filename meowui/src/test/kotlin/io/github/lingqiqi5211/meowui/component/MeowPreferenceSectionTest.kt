package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 分区收集的语义：渲染出来的行，就是 content 这一趟声明的那些行。
 *
 * content 是 composable lambda，它捕获的值变了时只有它自己重跑，持有条目列表的分区主体不重组。
 * 曾经的处理是把这趟结果并入旧条目，于是换一批行时旧行留在原位、新行追加在后面——一个分组里同时
 * 挂着两套内容。两套风格都测，因为收集发生在风格分发之前。
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class MeowPreferenceSectionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun materialSectionShowsOnlyTheRowsDeclaredNow() {
        assertRowsAreReplaced(MeowUiStyle.MaterialExpressive)
    }

    @Test
    fun miuixSectionShowsOnlyTheRowsDeclaredNow() {
        assertRowsAreReplaced(MeowUiStyle.Miuix)
    }

    @Test
    fun hidingEveryRowEmptiesTheGroup() {
        var visible by mutableStateOf(true)
        compose.setContent {
            MeowTheme(appearance = appearance(MeowUiStyle.MaterialExpressive)) {
                MeowPreferenceSection(modifier = Modifier.testTag(SECTION)) {
                    // Plain content rather than a typed row: what is under test is whether the
                    // group notices the item disappearing, not how a preference row draws.
                    item(key = "only", visible = visible) {
                        Text(
                            text = "only",
                            modifier = Modifier
                                .padding(16.dp)
                                .testTag("$SECTION.row.only"),
                        )
                    }
                }
            }
        }

        compose.onNodeWithTag("$SECTION.row.only").assertIsDisplayed()

        // A row that comes and goes is declared with `visible`, which is also how content
        // announces that it re-ran — so the group can go empty.
        compose.runOnIdle { visible = false }
        compose.waitForIdle()

        compose.onAllNodesWithTag("$SECTION.row.only").assertCountEquals(0)
        compose.onAllNodesWithTag(SECTION).assertCountEquals(0)
    }

    private fun assertRowsAreReplaced(style: MeowUiStyle) {
        var titles by mutableStateOf(listOf("first", "second"))
        compose.setContent {
            MeowTheme(appearance = appearance(style)) {
                MeowPreferenceSection(modifier = Modifier.testTag(SECTION)) {
                    titles.forEach { title ->
                        MeowActionPreference(
                            title = title,
                            modifier = Modifier.testTag("$SECTION.row.$title"),
                            onClick = {},
                        )
                    }
                    MeowActionPreference(
                        title = "always",
                        modifier = Modifier.testTag("$SECTION.row.always"),
                        onClick = {},
                    )
                }
            }
        }

        compose.onNodeWithTag("$SECTION.row.first").assertIsDisplayed()
        compose.onNodeWithTag("$SECTION.row.second").assertIsDisplayed()

        // Only the lambda's captured value changes: this is the lambda-only re-run that used
        // to accumulate.
        compose.runOnIdle { titles = listOf("third", "fourth") }
        compose.waitForIdle()

        compose.onAllNodesWithTag("$SECTION.row.first").assertCountEquals(0)
        compose.onAllNodesWithTag("$SECTION.row.second").assertCountEquals(0)
        compose.onNodeWithTag("$SECTION.row.third").assertIsDisplayed()
        compose.onNodeWithTag("$SECTION.row.fourth").assertIsDisplayed()
        // The row present in both contents stays once, in its own place.
        compose.onAllNodesWithTag("$SECTION.row.always").assertCountEquals(1)
    }

    @Test
    fun sectionKeepsRowsWhenOnlyANestedSlotReRuns() {
        var value by mutableStateOf(0)
        var title by mutableStateOf("A")
        compose.setContent {
            MeowTheme(appearance = appearance(MeowUiStyle.MaterialExpressive)) {
                CompositionLocalProvider(nestedSlotValue provides value) {
                    MeowPreferenceSection(title = title, modifier = Modifier.testTag(SECTION)) {
                        item(key = "fixed") {
                            Text(
                                text = "fixed",
                                modifier = Modifier.testTag(FIXED_ROW).padding(8.dp),
                            )
                        }
                        NestedSlot()
                    }
                }
            }
        }

        compose.onNodeWithTag(FIXED_ROW).assertIsDisplayed()
        compose.onNodeWithTag(NESTED_ROW).assertIsDisplayed()

        // 主体因为 title 重组，content 整组可复用，只有槽因为读的值变了而单独重跑。
        compose.runOnIdle {
            title = "B"
            value = 1
        }
        compose.waitForIdle()

        compose.onNodeWithTag(FIXED_ROW).assertIsDisplayed()
        compose.onNodeWithTag(NESTED_ROW).assertIsDisplayed()
    }

    /** Fixed light theme with no dynamic colour: what is under test is the row set. */
    private fun appearance(style: MeowUiStyle): MeowAppearance =
        MeowAppearance(
            style = style,
            themeMode = MeowThemeMode.Light,
            dynamicColor = false,
        )

    private companion object {
        const val SECTION = "meowui.preferenceSection"
    }
}

/**
 * 分区里嵌着一个自己会重跑的槽：调用侧传进来的 items、读了 CompositionLocal 的行，都是这种。
 *
 * 这种槽失效时，Compose 会复用 content 整组、只重跑槽本身。那一趟只有槽里的行会进来，
 * 把它当成完整的一趟提交，分组就只剩那一行——真机上表现为转屏之后一整块配置项没了。
 */
private val nestedSlotValue = compositionLocalOf { 0 }

@Composable
private fun MeowPreferenceSectionScope.NestedSlot() {
    val value = nestedSlotValue.current
    item(key = "nested") {
        Text(
            text = "nested $value",
            modifier = Modifier.testTag(NESTED_ROW).padding(8.dp),
        )
    }
}

private const val FIXED_ROW = "meowui.preferenceSection.row.fixed"

private const val NESTED_ROW = "meowui.preferenceSection.row.nested"
