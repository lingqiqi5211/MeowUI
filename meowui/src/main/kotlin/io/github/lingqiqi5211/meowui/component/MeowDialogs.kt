package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.Checkbox as MaterialCheckbox
import androidx.compose.material3.ButtonDefaults as MaterialButtonDefaults
import androidx.compose.material3.CircularProgressIndicator as MaterialCircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton as MaterialRadioButton
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.material3.TextField as MaterialTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox as MiuixCheckbox
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import top.yukonga.miuix.kmp.preference.RadioButtonPreference as MiuixRadioButtonPreference
import top.yukonga.miuix.kmp.window.WindowDialog

/** Visual emphasis used by [MeowAlertDialog]. */
enum class MeowAlertStyle {
    Standard,
    Warning,
}

/**
 * Shows a controlled alert or confirmation dialog.
 *
 * [onConfirm] does not hide the dialog automatically. Update [show] from the caller after handling
 * the action. Set [cancelText] to `null` for a one-button alert.
 *
 * The cancel button reports [onDismissRequest] by default, which is right when
 * cancelling and dismissing mean the same thing. Supply [onCancel] when they differ
 * — an unsaved-changes prompt where the button discards but a back press or an
 * outside tap keeps the user on the page.
 *
 * The dialog builds its own buttons in the active style, so [confirmModifier] and
 * [cancelModifier] are how the call site reaches them — to tag them for tests, or
 * to constrain them.
 */
@Composable
fun MeowAlertDialog(
    show: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmText: String = "OK",
    cancelText: String? = "Cancel",
    style: MeowAlertStyle = MeowAlertStyle.Standard,
    confirmEnabled: Boolean = true,
    onCancel: (() -> Unit)? = null,
    confirmModifier: Modifier = Modifier,
    cancelModifier: Modifier = Modifier,
) {
    val cancelAction = onCancel ?: onDismissRequest
    MeowStyleContent(
        materialExpressive = {
            if (show) {
                MaterialAlertDialog(
                    title = title,
                    message = message,
                    confirmText = confirmText,
                    cancelText = cancelText,
                    style = style,
                    confirmEnabled = confirmEnabled,
                    confirmModifier = confirmModifier,
                    cancelModifier = cancelModifier,
                    onConfirm = onConfirm,
                    onCancel = cancelAction,
                    onDismissRequest = onDismissRequest,
                )
            }
        },
        miuix = {
            MiuixAlertDialog(
                show = show,
                title = title,
                message = message,
                confirmText = confirmText,
                cancelText = cancelText,
                style = style,
                confirmEnabled = confirmEnabled,
                confirmModifier = confirmModifier,
                cancelModifier = cancelModifier,
                onConfirm = onConfirm,
                onCancel = cancelAction,
                onDismissRequest = onDismissRequest,
            )
        },
    )
}

/**
 * Shows a single-choice dialog whose selection is committed only after confirmation.
 *
 * [onSelected] is invoked only when the confirm button is pressed. Dismissing the dialog discards
 * the temporary selection.
 */
@Composable
fun <T> MeowSingleChoiceDialog(
    show: Boolean,
    title: String,
    selected: T,
    options: List<T>,
    onSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    optionLabel: (T) -> String = { it.toString() },
    cancelText: String = "Cancel",
    confirmText: String = "OK",
    confirmModifier: Modifier = Modifier,
    cancelModifier: Modifier = Modifier,
) {
    // 只按 show 重置：dialog 打开期间外部值（如远程设置回推）变化不清掉用户的临时选择。
    var draft by remember(show) { mutableStateOf(selected) }
    val confirmEnabled = options.any { it == draft }

    MeowStyleContent(
        materialExpressive = {
            if (show) {
                MaterialSingleChoiceDialog(
                    title = title,
                    selected = draft,
                    options = options,
                    optionLabel = optionLabel,
                    confirmText = confirmText,
                    cancelText = cancelText,
                    confirmEnabled = confirmEnabled,
                    confirmModifier = confirmModifier,
                    cancelModifier = cancelModifier,
                    onSelected = { draft = it },
                    onConfirm = { if (confirmEnabled) onSelected(draft) },
                    onDismissRequest = onDismissRequest,
                )
            }
        },
        miuix = {
            MiuixSingleChoiceDialog(
                show = show,
                title = title,
                selected = draft,
                options = options,
                optionLabel = optionLabel,
                confirmText = confirmText,
                cancelText = cancelText,
                confirmEnabled = confirmEnabled,
                confirmModifier = confirmModifier,
                cancelModifier = cancelModifier,
                onSelected = { draft = it },
                onConfirm = { if (confirmEnabled) onSelected(draft) },
                onDismissRequest = onDismissRequest,
            )
        },
    )
}

/**
 * 多选对话框，确认后一次性提交选择。
 *
 * 与 [MeowSingleChoiceDialog] 的区别只在于可以选多项：草稿同样只在确认时提交，取消或点外
 * 面关掉都丢弃。适合「导出/分享时包含哪些字段」这类每次都要确认一遍的场景。
 *
 * [onConfirmed] 拿到的是确认那一刻的完整选择集合，不是增量。
 */
@Composable
fun <T> MeowMultiChoiceDialog(
    show: Boolean,
    title: String,
    selected: Set<T>,
    options: List<T>,
    onConfirmed: (Set<T>) -> Unit,
    onDismissRequest: () -> Unit,
    optionLabel: (T) -> String = { it.toString() },
    cancelText: String = "Cancel",
    confirmText: String = "OK",
    confirmModifier: Modifier = Modifier,
    cancelModifier: Modifier = Modifier,
) {
    // 只按 show 重置，理由同 MeowSingleChoiceDialog：打开期间外部值变化不清掉临时选择。
    var draft by remember(show) { mutableStateOf(selected) }
    val toggle: (T) -> Unit = { option ->
        draft = if (option in draft) draft - option else draft + option
    }

    MeowStyleContent(
        materialExpressive = {
            if (show) {
                MaterialMultiChoiceDialog(
                    title = title,
                    selected = draft,
                    options = options,
                    optionLabel = optionLabel,
                    confirmText = confirmText,
                    cancelText = cancelText,
                    confirmModifier = confirmModifier,
                    cancelModifier = cancelModifier,
                    onToggled = toggle,
                    onConfirm = { onConfirmed(draft) },
                    onDismissRequest = onDismissRequest,
                )
            }
        },
        miuix = {
            MiuixMultiChoiceDialog(
                show = show,
                title = title,
                selected = draft,
                options = options,
                optionLabel = optionLabel,
                confirmText = confirmText,
                cancelText = cancelText,
                confirmModifier = confirmModifier,
                cancelModifier = cancelModifier,
                onToggled = toggle,
                onConfirm = { onConfirmed(draft) },
                onDismissRequest = onDismissRequest,
            )
        },
    )
}

/**
 * Shows a text-input dialog with optional blank-value and custom validation rules.
 *
 * [validator] returns `null` for a valid value or an error message for an invalid value. Invalid
 * input displays the message and disables confirmation. [onConfirm] does not hide the dialog
 * automatically.
 */
@Composable
fun MeowTextInputDialog(
    show: Boolean,
    title: String,
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
    placeholder: String = "",
    confirmText: String = "OK",
    cancelText: String = "Cancel",
    singleLine: Boolean = true,
    minLines: Int = if (singleLine) 1 else 3,
    maxLines: Int = if (singleLine) 1 else 6,
    allowBlank: Boolean = true,
    blankErrorText: String = MeowDefaultBlankErrorText,
    validator: (String) -> String? = { null },
    confirmModifier: Modifier = Modifier,
    cancelModifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
) {
    // 只按 show 重置：打开期间 initialValue 变化不丢弃正在输入的内容。
    var draft by remember(show) { mutableStateOf(initialValue) }
    val resolvedMinLines = if (singleLine) 1 else minLines.coerceAtLeast(1)
    val resolvedMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(resolvedMinLines)
    val validationError = validateTextInput(
        value = draft,
        allowBlank = allowBlank,
        blankErrorText = blankErrorText,
        validator = validator,
    )
    val confirmEnabled = validationError == null

    MeowStyleContent(
        materialExpressive = {
            if (show) {
                MaterialTextInputDialog(
                    title = title,
                    value = draft,
                    placeholder = placeholder,
                    validationError = validationError,
                    confirmText = confirmText,
                    cancelText = cancelText,
                    singleLine = singleLine,
                    minLines = resolvedMinLines,
                    maxLines = resolvedMaxLines,
                    confirmEnabled = confirmEnabled,
                    confirmModifier = confirmModifier,
                    cancelModifier = cancelModifier,
                    fieldModifier = fieldModifier,
                    onValueChange = { draft = it },
                    onConfirm = { if (confirmEnabled) onConfirm(draft) },
                    onDismissRequest = onDismissRequest,
                )
            }
        },
        miuix = {
            MiuixTextInputDialog(
                show = show,
                title = title,
                value = draft,
                placeholder = placeholder,
                validationError = validationError,
                confirmText = confirmText,
                cancelText = cancelText,
                singleLine = singleLine,
                minLines = resolvedMinLines,
                maxLines = resolvedMaxLines,
                confirmEnabled = confirmEnabled,
                confirmModifier = confirmModifier,
                cancelModifier = cancelModifier,
                fieldModifier = fieldModifier,
                onValueChange = { draft = it },
                onConfirm = { if (confirmEnabled) onConfirm(draft) },
                onDismissRequest = onDismissRequest,
            )
        },
    )
}

/**
 * Shows a loading dialog. It cannot be dismissed by back press or outside tap unless
 * [onDismissRequest] is provided.
 */
@Composable
fun MeowLoadingDialog(
    show: Boolean,
    title: String? = null,
    message: String? = null,
    onDismissRequest: (() -> Unit)? = null,
) {
    MeowStyleContent(
        materialExpressive = {
            if (show) {
                MaterialLoadingDialog(
                    title = title,
                    message = message,
                    onDismissRequest = onDismissRequest,
                )
            }
        },
        miuix = {
            MiuixLoadingDialog(
                show = show,
                title = title,
                message = message,
                onDismissRequest = onDismissRequest,
            )
        },
    )
}

@Composable
private fun MaterialAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String?,
    style: MeowAlertStyle,
    confirmEnabled: Boolean,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val isWarning = style == MeowAlertStyle.Warning

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            MaterialText(
                text = title,
                color = if (isWarning) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        },
        text = {
            MaterialText(message)
        },
        confirmButton = {
            MaterialButton(
                onClick = onConfirm,
                modifier = confirmModifier,
                enabled = confirmEnabled,
                colors = if (isWarning) {
                    MaterialButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    MaterialButtonDefaults.buttonColors()
                },
            ) {
                MaterialText(confirmText)
            }
        },
        dismissButton = {
            cancelText?.let {
                MaterialTextButton(onClick = onCancel, modifier = cancelModifier) {
                    MaterialText(it)
                }
            }
        },
    )
}

@Composable
private fun MiuixAlertDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String?,
    style: MeowAlertStyle,
    confirmEnabled: Boolean,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    WindowDialog(
        show = show,
        title = title,
        titleColor = if (style == MeowAlertStyle.Warning) {
            MeowTheme.colors.error
        } else {
            MeowTheme.colors.onSurface
        },
        summary = message,
        onDismissRequest = onDismissRequest,
    ) {
        MiuixDialogButtons(
            confirmText = confirmText,
            cancelText = cancelText,
            onConfirm = onConfirm,
            onCancel = onCancel,
            confirmEnabled = confirmEnabled,
            confirmModifier = confirmModifier,
            cancelModifier = cancelModifier,
        )
    }
}

@Composable
private fun <T> MaterialSingleChoiceDialog(
    title: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    confirmText: String,
    cancelText: String,
    confirmEnabled: Boolean,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    onSelected: (T) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { MaterialText(title) },
        text = {
            MaterialChoiceList(
                selected = selected,
                options = options,
                optionLabel = optionLabel,
                onSelected = onSelected,
            )
        },
        confirmButton = {
            MaterialButton(
                onClick = onConfirm,
                modifier = confirmModifier,
                enabled = confirmEnabled,
            ) {
                MaterialText(confirmText)
            }
        },
        dismissButton = {
            MaterialTextButton(onClick = onDismissRequest, modifier = cancelModifier) {
                MaterialText(cancelText)
            }
        },
    )
}

@Composable
private fun <T> MiuixSingleChoiceDialog(
    show: Boolean,
    title: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    confirmText: String,
    cancelText: String,
    confirmEnabled: Boolean,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    onSelected: (T) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    WindowDialog(
        show = show,
        title = title,
        insideMargin = DpSize(0.dp, 24.dp),
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MiuixChoiceList(
                selected = selected,
                options = options,
                optionLabel = optionLabel,
                onSelected = onSelected,
            )
            Spacer(Modifier.height(12.dp))
            MiuixDialogButtons(
                confirmText = confirmText,
                cancelText = cancelText,
                confirmEnabled = confirmEnabled,
                onConfirm = onConfirm,
                onCancel = onDismissRequest,
                modifier = Modifier.padding(horizontal = 24.dp),
                confirmModifier = confirmModifier,
                cancelModifier = cancelModifier,
            )
        }
    }
}

@Composable
private fun MaterialTextInputDialog(
    title: String,
    value: String,
    placeholder: String,
    validationError: String?,
    confirmText: String,
    cancelText: String,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    confirmEnabled: Boolean,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    fieldModifier: Modifier,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { MaterialText(title) },
        text = {
            MaterialTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(fieldModifier),
                placeholder = placeholder.takeIf { it.isNotBlank() }?.let { hint ->
                    { MaterialText(hint) }
                },
                supportingText = validationError?.let { error ->
                    { MaterialText(error) }
                },
                isError = validationError != null,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
            )
        },
        confirmButton = {
            MaterialButton(
                onClick = onConfirm,
                modifier = confirmModifier,
                enabled = confirmEnabled,
            ) {
                MaterialText(confirmText)
            }
        },
        dismissButton = {
            MaterialTextButton(onClick = onDismissRequest, modifier = cancelModifier) {
                MaterialText(cancelText)
            }
        },
    )
}

@Composable
private fun MiuixTextInputDialog(
    show: Boolean,
    title: String,
    value: String,
    placeholder: String,
    validationError: String?,
    confirmText: String,
    cancelText: String,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    confirmEnabled: Boolean,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    fieldModifier: Modifier,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    WindowDialog(
        show = show,
        title = title,
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MiuixTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(fieldModifier),
                colors = MiuixTextFieldDefaults.textFieldColors(
                    labelColor = if (validationError == null) {
                        MeowTheme.colors.onSurfaceVariant
                    } else {
                        MeowTheme.colors.error
                    },
                    borderColor = if (validationError == null) {
                        MeowTheme.colors.primary
                    } else {
                        MeowTheme.colors.error
                    },
                ),
                label = placeholder,
                useLabelAsPlaceholder = true,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
            )
            validationError?.let {
                Spacer(Modifier.height(8.dp))
                MiuixText(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    color = MeowTheme.colors.error,
                    style = MeowTheme.typography.summary,
                )
            }
            Spacer(Modifier.height(20.dp))
            MiuixDialogButtons(
                confirmText = confirmText,
                cancelText = cancelText,
                confirmEnabled = confirmEnabled,
                onConfirm = onConfirm,
                onCancel = onDismissRequest,
                confirmModifier = confirmModifier,
                cancelModifier = cancelModifier,
            )
        }
    }
}

@Composable
private fun MaterialLoadingDialog(
    title: String?,
    message: String?,
    onDismissRequest: (() -> Unit)?,
) {
    val dismissible = onDismissRequest != null

    AlertDialog(
        onDismissRequest = { onDismissRequest?.invoke() },
        title = title?.let { dialogTitle ->
            { MaterialText(dialogTitle) }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                message?.takeIf { it.isNotBlank() }?.let {
                    MaterialText(
                        text = it,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                }
                MaterialCircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                )
            }
        },
        confirmButton = {},
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible,
        ),
    )
}

@Composable
private fun MiuixLoadingDialog(
    show: Boolean,
    title: String?,
    message: String?,
    onDismissRequest: (() -> Unit)?,
) {
    WindowDialog(
        show = show,
        title = title,
        summary = message?.takeIf { it.isNotBlank() },
        onDismissRequest = onDismissRequest,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            MiuixCircularProgressIndicator(size = 36.dp)
        }
    }
}

@Composable
private fun <T> MaterialChoiceList(
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .verticalScroll(rememberScrollState())
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            key(index, option) {
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onSelected(option) },
                        )
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MaterialRadioButton(
                        selected = isSelected,
                        onClick = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    MaterialText(
                        text = optionLabel(option),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> MaterialMultiChoiceDialog(
    title: String,
    selected: Set<T>,
    options: List<T>,
    optionLabel: (T) -> String,
    confirmText: String,
    cancelText: String,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    onToggled: (T) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { MaterialText(title) },
        text = {
            MaterialCheckboxList(
                selected = selected,
                options = options,
                optionLabel = optionLabel,
                onToggled = onToggled,
            )
        },
        confirmButton = {
            MaterialButton(onClick = onConfirm, modifier = confirmModifier) {
                MaterialText(confirmText)
            }
        },
        dismissButton = {
            MaterialTextButton(onClick = onDismissRequest, modifier = cancelModifier) {
                MaterialText(cancelText)
            }
        },
    )
}

@Composable
private fun <T> MiuixMultiChoiceDialog(
    show: Boolean,
    title: String,
    selected: Set<T>,
    options: List<T>,
    optionLabel: (T) -> String,
    confirmText: String,
    cancelText: String,
    confirmModifier: Modifier,
    cancelModifier: Modifier,
    onToggled: (T) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    WindowDialog(
        show = show,
        title = title,
        insideMargin = DpSize(0.dp, 24.dp),
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MiuixCheckboxList(
                selected = selected,
                options = options,
                optionLabel = optionLabel,
                onToggled = onToggled,
            )
            Spacer(Modifier.height(12.dp))
            MiuixDialogButtons(
                confirmText = confirmText,
                cancelText = cancelText,
                onConfirm = onConfirm,
                onCancel = onDismissRequest,
                modifier = Modifier.padding(horizontal = 24.dp),
                confirmModifier = confirmModifier,
                cancelModifier = cancelModifier,
            )
        }
    }
}

@Composable
private fun <T> MaterialCheckboxList(
    selected: Set<T>,
    options: List<T>,
    optionLabel: (T) -> String,
    onToggled: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        options.forEachIndexed { index, option ->
            key(index, option) {
                val isChecked = option in selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .toggleable(
                            value = isChecked,
                            role = Role.Checkbox,
                            onValueChange = { onToggled(option) },
                        )
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // null callback: the whole row is the target, and a checkbox that also
                    // handled clicks would announce itself separately to a screen reader.
                    MaterialCheckbox(checked = isChecked, onCheckedChange = null)
                    Spacer(Modifier.width(12.dp))
                    MaterialText(
                        text = optionLabel(option),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> MiuixCheckboxList(
    selected: Set<T>,
    options: List<T>,
    optionLabel: (T) -> String,
    onToggled: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        options.forEachIndexed { index, option ->
            key(index, option) {
                val isChecked = option in selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .toggleable(
                            value = isChecked,
                            role = Role.Checkbox,
                            onValueChange = { onToggled(option) },
                        )
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiuixCheckbox(
                        state = if (isChecked) ToggleableState.On else ToggleableState.Off,
                        onClick = null,
                    )
                    Spacer(Modifier.width(12.dp))
                    MiuixText(text = optionLabel(option), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun <T> MiuixChoiceList(
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .verticalScroll(rememberScrollState())
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            key(index, option) {
                MiuixRadioButtonPreference(
                    title = optionLabel(option),
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun MiuixDialogButtons(
    confirmText: String,
    cancelText: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmModifier: Modifier = Modifier,
    cancelModifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cancelText?.let {
            MiuixButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .then(cancelModifier),
            ) {
                MiuixText(
                    text = it,
                    style = MeowTheme.typography.button,
                )
            }
        }
        MiuixButton(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .then(confirmModifier),
            enabled = confirmEnabled,
            colors = MiuixButtonDefaults.buttonColorsPrimary(),
        ) {
            MiuixText(
                text = confirmText,
                style = MeowTheme.typography.button,
            )
        }
    }
}

private fun validateTextInput(
    value: String,
    allowBlank: Boolean,
    blankErrorText: String,
    validator: (String) -> String?,
): String? = when {
    !allowBlank && value.isBlank() -> blankErrorText
    else -> validator(value)
}
