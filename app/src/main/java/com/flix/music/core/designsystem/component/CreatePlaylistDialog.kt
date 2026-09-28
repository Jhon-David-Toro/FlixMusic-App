package com.flix.music.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.Playlist

/**
 * "Nueva playlist" (design `plcreate`): name with a "11 / 60" counter, optional
 * description and a note that the cover is automatic. "Crear" stays disabled until
 * the name has text.
 */
@Composable
fun CreatePlaylistDialog(
    onCreate: (name: String, description: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val nameFocus = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DIALOG_SHAPE,
            color = FlixTheme.colors.surfaceElevated,
            contentColor = FlixTheme.colors.textPrimary,
        ) {
            Column(Modifier.padding(FlixTheme.spacing.xl)) {
                Text(
                    text = stringResource(R.string.playlist_create_title),
                    style = FlixTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() },
                )
                FlixTextField(
                    value = name,
                    onValueChange = { newName -> name = newName },
                    label = stringResource(R.string.playlist_field_name),
                    helper = stringResource(R.string.playlist_name_counter, name.length, Playlist.MAX_NAME_LENGTH),
                    maxLength = Playlist.MAX_NAME_LENGTH,
                    modifier = Modifier
                        .padding(top = FlixTheme.spacing.lg)
                        .focusRequester(nameFocus),
                )
                FlixTextField(
                    value = description,
                    onValueChange = { newDescription -> description = newDescription },
                    label = stringResource(R.string.playlist_field_description_optional),
                    placeholder = stringResource(R.string.playlist_description_placeholder),
                    singleLine = false,
                    modifier = Modifier.padding(top = FlixTheme.spacing.md),
                )
                Row(
                    modifier = Modifier.padding(top = FlixTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = null,
                        tint = FlixTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.playlist_cover_automatic),
                        style = FlixTheme.typography.caption,
                        color = FlixTheme.colors.textSecondary,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            style = FlixTheme.typography.label,
                            color = FlixTheme.colors.primaryContent,
                        )
                    }
                    FlixPrimaryButton(
                        text = stringResource(R.string.playlist_create_confirm),
                        onClick = { onCreate(name, description) },
                        enabled = name.isNotBlank(),
                    )
                }
            }
        }
    }
    LaunchedEffect(nameFocus) { nameFocus.requestFocus() }
}

private val DIALOG_SHAPE = RoundedCornerShape(22.dp)

@Preview(name = "Nueva playlist", widthDp = 360, heightDp = 520)
@Composable
private fun CreatePlaylistDialogPreview() {
    FlixPreview { CreatePlaylistDialog(onCreate = { _, _ -> }, onDismiss = {}) }
}
