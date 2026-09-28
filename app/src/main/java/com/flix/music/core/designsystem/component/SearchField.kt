package com.flix.music.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixTheme

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    clearLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val flixColors = FlixTheme.colors

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f),
        enabled = enabled,
        placeholder = { Text(placeholder, style = FlixTheme.typography.body) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = clearLabel)
                }
            }
        },
        singleLine = true,
        shape = FlixTheme.shapes.md,
        textStyle = FlixTheme.typography.body,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = flixColors.surfaceVariant,
            focusedContainerColor = flixColors.surface,
            disabledContainerColor = flixColors.surfaceVariant,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = flixColors.textPrimary,
            disabledBorderColor = Color.Transparent,
            cursorColor = flixColors.primary,
            focusedTextColor = flixColors.textPrimary,
            unfocusedTextColor = flixColors.textPrimary,
            disabledTextColor = flixColors.textPrimary,
            focusedPlaceholderColor = flixColors.textTertiary,
            unfocusedPlaceholderColor = flixColors.textTertiary,
            disabledPlaceholderColor = flixColors.textTertiary,
            focusedLeadingIconColor = flixColors.textSecondary,
            unfocusedLeadingIconColor = flixColors.textSecondary,
            disabledLeadingIconColor = flixColors.textSecondary,
            focusedTrailingIconColor = flixColors.textSecondary,
            unfocusedTrailingIconColor = flixColors.textSecondary,
        ),
    )
}
