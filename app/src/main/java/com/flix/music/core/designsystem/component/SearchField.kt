package com.flix.music.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val colorFlix = FlixTheme.colors

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = clearLabel )
                }
            }
        },
        singleLine = true,
        shape = FlixTheme.shapes.md,
        textStyle = FlixTheme.typography.body,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorFlix.surfaceVariant,
            unfocusedContainerColor = colorFlix.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = colorFlix.primary,
            focusedTextColor = colorFlix.textPrimary,
            unfocusedTextColor = colorFlix.textPrimary,
            focusedPlaceholderColor = colorFlix.textTertiary,
            unfocusedPlaceholderColor = colorFlix.textTertiary,
            focusedLeadingIconColor = colorFlix.textSecondary,
            unfocusedLeadingIconColor = colorFlix.textSecondary,
            focusedTrailingIconColor = colorFlix.textSecondary,
            unfocusedTrailingIconColor = colorFlix.textSecondary,
        ),
    )
}