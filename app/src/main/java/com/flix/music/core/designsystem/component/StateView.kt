package com.flix.music.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.FlixError

@Composable
fun StateView(
    @DrawableRes icon: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FlixTheme.spacing.x4, vertical = FlixTheme.spacing.x4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FlixTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(FlixTheme.colors.surfaceVariant, FlixTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = FlixTheme.colors.textSecondary,
                modifier = Modifier.size(34.dp),
            )
        }
        Text(
            text = title,
            style = FlixTheme.typography.title,
            color = FlixTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = FlixTheme.typography.bodySmall,
            color = FlixTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                shape = FlixTheme.shapes.pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FlixTheme.colors.primary,
                    contentColor = FlixTheme.colors.onPrimary,
                ),
            ) {
                Text(actionLabel, style = FlixTheme.typography.label)
            }
        }
    }
}

@Composable
fun ErrorStateView(
    error: FlixError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, title, body) = error.toCopy()
    StateView(
        icon = icon,
        title = stringResource(title),
        body = stringResource(body),
        actionLabel = stringResource(R.string.action_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

private fun FlixError.toCopy(): Triple<Int, Int, Int> = when (this) {
    FlixError.NoConnection ->
        Triple(R.drawable.ic_cloud_off, R.string.error_no_connection_title, R.string.error_no_connection_body)
    FlixError.ServerUnreachable ->
        Triple(R.drawable.ic_dns, R.string.error_server_title, R.string.error_server_body)
    FlixError.Timeout ->
        Triple(R.drawable.ic_cloud_off, R.string.error_timeout_title, R.string.error_timeout_body)
    FlixError.Upstream ->
        Triple(R.drawable.ic_cloud_off, R.string.error_upstream_title, R.string.error_upstream_body)
    FlixError.Unknown ->
        Triple(R.drawable.ic_cloud_off, R.string.error_unknown_title, R.string.error_unknown_body)
}