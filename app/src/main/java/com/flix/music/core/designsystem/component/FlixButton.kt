package com.flix.music.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.flix.music.core.designsystem.theme.FlixTheme

/** Primary button (`.btn.b1`): Lumen violet pill. At most one per screen. */
@Composable
fun FlixPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
) {
    FlixButton(
        text = text,
        onClick = onClick,
        containerColor = FlixTheme.colors.primary,
        contentColor = FlixTheme.colors.onPrimary,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
    )
}

/** Secondary button (`.btn.b2`): surfaceVariant pill, next to a primary action. */
@Composable
fun FlixSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
) {
    FlixButton(
        text = text,
        onClick = onClick,
        containerColor = FlixTheme.colors.surfaceVariant,
        contentColor = FlixTheme.colors.textPrimary,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
    )
}

/** Disabled buttons fade as a whole to 38 % (design: Buttons → disabled). */
@Composable
private fun FlixButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier,
    @DrawableRes icon: Int?,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = modifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
        enabled = enabled,
        shape = FlixTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor,
        ),
        contentPadding = if (icon != null) ButtonDefaults.ButtonWithIconContentPadding else ButtonDefaults.ContentPadding,
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(text, style = FlixTheme.typography.label)
    }
}

private const val DISABLED_ALPHA = 0.38f
