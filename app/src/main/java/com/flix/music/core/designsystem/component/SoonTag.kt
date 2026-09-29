package com.flix.music.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixTheme

/** "PRÓXIMAMENTE" pill (design `.soon`) for features that are designed but not active yet. */
@Composable
fun SoonTag(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.coming_soon).uppercase(),
        style = FlixTheme.typography.overline.copy(fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold),
        color = FlixTheme.colors.textSecondary,
        modifier = modifier
            .border(1.dp, FlixTheme.colors.divider, FlixTheme.shapes.pill)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}
