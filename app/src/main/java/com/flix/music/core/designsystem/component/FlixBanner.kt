package com.flix.music.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixTheme

/**
 * Banner (design `.banner`): surfaceVariant, radius md, an 18dp icon and 12sp text.
 * The default is `.banner.info` (info icon in the info color), used where content may
 * be incomplete, such as the album tracks derived from search.
 */
@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.ic_info,
    iconTint: Color = FlixTheme.colors.info,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FlixTheme.shapes.md)
            .background(FlixTheme.colors.surfaceVariant)
            .padding(horizontal = FlixTheme.spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
        Text(text = text, style = FlixTheme.typography.caption, color = FlixTheme.colors.textPrimary)
    }
}
