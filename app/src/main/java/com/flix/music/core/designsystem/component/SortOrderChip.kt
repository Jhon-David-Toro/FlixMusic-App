package com.flix.music.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flix.music.R
import com.flix.music.core.model.SortOrder

/** "⇅ Recientes / A–Z" chip; each tap switches to the other order. */
@Composable
fun SortOrderChip(
    order: SortOrder,
    onOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (order) {
        SortOrder.Recent -> stringResource(R.string.sort_recent)
        SortOrder.Alphabetical -> stringResource(R.string.sort_alphabetical)
    }
    FlixChip(
        label = label,
        onClick = {
            onOrderChange(if (order == SortOrder.Recent) SortOrder.Alphabetical else SortOrder.Recent)
        },
        leadingIcon = R.drawable.ic_swap_vert,
        modifier = modifier,
    )
}
