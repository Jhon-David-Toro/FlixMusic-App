package com.flix.music.feature.song

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.flix.music.R
import com.flix.music.core.designsystem.component.ErrorStateView
import com.flix.music.core.designsystem.component.FavoriteButton
import com.flix.music.core.designsystem.component.FlixOutlinedButton
import com.flix.music.core.designsystem.component.FlixPrimaryButton
import com.flix.music.core.designsystem.component.FlixTopAppBar
import com.flix.music.core.designsystem.component.NowPlayingMarker
import com.flix.music.core.designsystem.component.SectionHeader
import com.flix.music.core.designsystem.component.SkeletonBox
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.util.toClock
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.AlbumRef
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import com.flix.music.core.ui.LocalDetailNavigator
import com.flix.music.core.ui.LocalTrackMenu
import kotlin.time.Duration.Companion.seconds

/** Every user action on the song screen. Defaults do nothing, which keeps previews short. */
data class SongActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onPlay: () -> Unit = {},
    val onToggleFavorite: () -> Unit = {},
    val onAddToPlaylist: (Track) -> Unit = {},
    val onShare: (Track) -> Unit = {},
    val onMore: (Track) -> Unit = {},
    val onOpenAlbum: (AlbumRef, ArtistRef?) -> Unit = { _, _ -> },
    val onOpenArtist: (ArtistRef) -> Unit = {},
    val onToggleFollow: () -> Unit = {},
    val onMoreLikeThisClick: (Track) -> Unit = {},
)

@Composable
fun SongScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SongViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()
    val trackMenu = LocalTrackMenu.current
    val detailNavigator = LocalDetailNavigator.current
    val context = LocalContext.current
    SongContent(
        uiState = uiState,
        nowPlaying = nowPlaying,
        actions = SongActions(
            onBack = onBack,
            onRetry = viewModel::onRetry,
            onPlay = viewModel::onPlay,
            onToggleFavorite = viewModel::onToggleFavorite,
            onAddToPlaylist = trackMenu::addToPlaylist,
            onShare = context::shareSong,
            onMore = { track -> trackMenu.open(track) },
            onOpenAlbum = detailNavigator::openAlbum,
            onOpenArtist = detailNavigator::openArtist,
            onToggleFollow = viewModel::onToggleFollow,
            onMoreLikeThisClick = viewModel::onMoreLikeThisClick,
        ),
        modifier = modifier,
    )
}

@Composable
fun SongContent(
    uiState: SongUiState,
    actions: SongActions,
    modifier: Modifier = Modifier,
    nowPlaying: NowPlayingMarker = NowPlayingMarker(),
) {
    val track = uiState.track
    Column(modifier.fillMaxSize()) {
        FlixTopAppBar(
            title = "",
            onBack = actions.onBack,
            actions = {
                if (track != null) {
                    IconButton(onClick = { actions.onShare(track) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = stringResource(R.string.action_share),
                            tint = FlixTheme.colors.textPrimary,
                        )
                    }
                    IconButton(onClick = { actions.onMore(track) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.action_more_options),
                            tint = FlixTheme.colors.textPrimary,
                        )
                    }
                }
            },
        )
        when {
            track != null -> SongDetails(uiState, track, actions, nowPlaying)
            uiState.error != null -> ErrorStateView(error = uiState.error, onRetry = actions.onRetry)
            else -> SongSkeleton()
        }
    }
}

@Composable
private fun SongDetails(uiState: SongUiState, track: Track, actions: SongActions, nowPlaying: NowPlayingMarker) {
    LazyColumn(contentPadding = PaddingValues(bottom = FlixTheme.spacing.xxl)) {
        item(key = "header") { SongHeader(uiState, track, actions) }
        track.album?.let { album ->
            item(key = "album") {
                SectionHeader(
                    title = stringResource(R.string.song_from_album),
                    modifier = Modifier.padding(top = SECTION_TOP),
                )
                CollectionRow(
                    title = album.name,
                    subtitle = stringResource(R.string.song_album_caption, track.artistNames),
                    artworkUrl = track.artworkUrl,
                    circular = false,
                    onClick = { actions.onOpenAlbum(album, track.artists.firstOrNull()) },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = FlixTheme.colors.textSecondary,
                    )
                }
            }
        }
        uiState.mainArtist?.let { artist ->
            item(key = "artist") {
                SectionHeader(
                    title = stringResource(R.string.type_artist),
                    modifier = Modifier.padding(top = SECTION_TOP),
                )
                CollectionRow(
                    title = artist.name,
                    subtitle = stringResource(R.string.type_artist),
                    artworkUrl = track.artworkUrl,
                    circular = true,
                    onClick = { actions.onOpenArtist(artist) },
                ) {
                    if (artist.id != null) {
                        FollowButton(isFollowing = uiState.isFollowingArtist, onClick = actions.onToggleFollow)
                    }
                }
            }
        }
        if (uiState.moreLikeThis.isNotEmpty()) {
            item(key = "more-header") {
                SectionHeader(
                    title = stringResource(R.string.song_more_like_this),
                    modifier = Modifier.padding(top = SECTION_TOP),
                )
            }
            items(uiState.moreLikeThis, key = { it.id }) { similar ->
                TrackRow(
                    title = similar.title,
                    subtitle = similar.artistNames,
                    artworkUrl = similar.artworkUrl,
                    onClick = { actions.onMoreLikeThisClick(similar) },
                    isCurrent = similar.id == nowPlaying.trackId,
                    isPlaying = nowPlaying.isPlaying,
                    onMoreClick = { actions.onMore(similar) },
                )
            }
        }
    }
}

/** Cover, title, artist (link), "Album · 6:57" and the actions row. */
@Composable
private fun SongHeader(uiState: SongUiState, track: Track, actions: SongActions) {
    Column(Modifier.padding(horizontal = FlixTheme.spacing.gutter)) {
        AsyncImage(
            model = track.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(horizontal = FlixTheme.spacing.xxl)
                .padding(top = FlixTheme.spacing.xs)
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(8.dp, COVER_SHAPE)
                .clip(COVER_SHAPE)
                .background(FlixTheme.colors.surfaceVariant),
        )
        Text(
            text = track.title,
            style = FlixTheme.typography.headline,
            color = FlixTheme.colors.textPrimary,
            modifier = Modifier
                .padding(top = 18.dp)
                .semantics { heading() },
        )
        Text(
            text = track.artistNames,
            style = FlixTheme.typography.label,
            color = FlixTheme.colors.textPrimary,
            modifier = Modifier
                .padding(top = 6.dp)
                .clickable(enabled = uiState.mainArtist != null) {
                    uiState.mainArtist?.let(actions.onOpenArtist)
                },
        )
        val details = listOfNotNull(track.album?.name, track.duration?.toClock()).joinToString(" · ")
        if (details.isNotEmpty()) {
            Text(
                text = details,
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Row(
            modifier = Modifier.padding(top = 14.dp, bottom = FlixTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FlixPrimaryButton(
                text = stringResource(R.string.playlist_play),
                onClick = actions.onPlay,
                icon = R.drawable.ic_play_arrow_filled,
                enabled = uiState.isPlayable,
            )
            FavoriteButton(
                isFavorite = uiState.isFavorite,
                onToggle = actions.onToggleFavorite,
                trackTitle = track.title,
                inactiveTint = FlixTheme.colors.textPrimary,
            )
            IconButton(onClick = { actions.onAddToPlaylist(track) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_playlist_add),
                    contentDescription = stringResource(R.string.add_to_playlist),
                    tint = FlixTheme.colors.textPrimary,
                )
            }
        }
        if (!uiState.isPlayable) {
            Text(
                text = stringResource(R.string.song_unavailable),
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textSecondary,
            )
        }
    }
}

/** A `.row` with a 48dp cover (square or circle), title, subtitle and a trailing slot. */
@Composable
private fun CollectionRow(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    circular: Boolean,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FlixTheme.spacing.rowHeight)
            .clickable(onClick = onClick)
            .padding(start = FlixTheme.spacing.gutter, end = FlixTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.itemGap),
    ) {
        AsyncImage(
            model = artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(if (circular) FlixTheme.shapes.circle else FlixTheme.shapes.sm)
                .background(FlixTheme.colors.surfaceVariant),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = FlixTheme.typography.subtitle,
                color = FlixTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(subtitle, style = FlixTheme.typography.bodySmall, color = FlixTheme.colors.textSecondary)
        }
        trailing()
    }
}

/** "Seguir" / "✓ Siguiendo" (design `.btn.bo.bsm`). */
@Composable
internal fun FollowButton(isFollowing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FlixOutlinedButton(
        text = stringResource(if (isFollowing) R.string.artist_following else R.string.artist_follow),
        onClick = onClick,
        icon = if (isFollowing) R.drawable.ic_check else null,
        modifier = modifier,
    )
}

@Composable
private fun SongSkeleton() {
    Column(Modifier.padding(horizontal = FlixTheme.spacing.gutter)) {
        SkeletonBox(
            Modifier
                .padding(horizontal = FlixTheme.spacing.xxl)
                .padding(top = FlixTheme.spacing.xs)
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        SkeletonBox(
            Modifier
                .padding(top = 18.dp)
                .fillMaxWidth(0.7f)
                .heightIn(min = 24.dp),
        )
        SkeletonBox(
            Modifier
                .padding(top = FlixTheme.spacing.sm)
                .fillMaxWidth(0.4f)
                .heightIn(min = 14.dp),
        )
    }
}

/** Shares the song as text with a YouTube Music link, which is where its id comes from. */
private fun Context.shareSong(track: Track) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getString(R.string.track_share_text, track.title, track.artistNames, track.id))
    }
    startActivity(Intent.createChooser(sendIntent, null))
}

private val COVER_SHAPE = RoundedCornerShape(16.dp)
private val SECTION_TOP = 22.dp

@Preview(name = "Canción", widthDp = 360, heightDp = 900)
@Composable
private fun SongPreview() {
    FlixPreview {
        SongContent(
            uiState = SongUiState(
                track = Track(
                    id = "1",
                    title = "Fade To Black",
                    artists = listOf(ArtistRef("m", "Metallica")),
                    album = AlbumRef("rtl", "Ride the Lightning"),
                    duration = 417.seconds,
                    artworkUrl = null,
                ),
                isFavorite = true,
                moreLikeThis = listOf(Track("2", "One", listOf(ArtistRef("m", "Metallica")), null, null, null)),
            ),
            actions = SongActions(),
        )
    }
}
