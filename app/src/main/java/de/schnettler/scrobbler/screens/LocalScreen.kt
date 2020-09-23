package de.schnettler.scrobbler.screens

import android.content.Intent
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Button
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onActive
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import de.schnettler.database.models.Scrobble
import de.schnettler.scrobble.MediaListenerService
import de.schnettler.scrobbler.UIAction
import de.schnettler.scrobbler.UIAction.ListingSelected
import de.schnettler.scrobbler.UIError
import de.schnettler.scrobbler.components.LoadingScreen
import de.schnettler.scrobbler.components.SwipeRefreshProgressIndicator
import de.schnettler.scrobbler.components.SwipeToRefreshLayout
import de.schnettler.scrobbler.screens.local.ConfirmDialog
import de.schnettler.scrobbler.screens.local.NowPlayingItem
import de.schnettler.scrobbler.screens.local.ScrobbleItem
import de.schnettler.scrobbler.screens.local.TrackEditDialog
import de.schnettler.scrobbler.util.ScrobbleAction
import de.schnettler.scrobbler.util.ScrobbleAction.DELETE
import de.schnettler.scrobbler.util.ScrobbleAction.EDIT
import de.schnettler.scrobbler.util.ScrobbleAction.OPEN
import de.schnettler.scrobbler.util.ScrobbleAction.SUBMIT
import de.schnettler.scrobbler.viewmodels.LocalViewModel
import timber.log.Timber

@Composable
fun LocalScreen(
    localViewModel: LocalViewModel,
    actionHandler: (UIAction) -> Unit,
    errorHandler: @Composable (UIError) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = ContextAmbient.current
    when (MediaListenerService.isEnabled(context)) {
        true -> Content(
            localViewModel = localViewModel,
            actionHandler = actionHandler,
            errorHandler = errorHandler,
            modifier = modifier
        )
        false -> Button(onClick = {
            context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }) {
            Text(text = "Enable")
        }
    }
    Timber.i("Started Service")
}

@Suppress("LongMethod")
@Composable
fun Content(
    localViewModel: LocalViewModel,
    actionHandler: (UIAction) -> Unit,
    errorHandler: @Composable (UIError) -> Unit,
    modifier: Modifier = Modifier,
) {
    onActive { localViewModel.startStream() }
    val recentTracksState by localViewModel.state.collectAsState()
    val cachedNumber by localViewModel.cachedScrobblesCOunt.collectAsState(initial = 0)
    var showEditDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val selectedTrack: MutableState<Scrobble?> = remember { mutableStateOf(null) }
    if (recentTracksState.isError) {
        errorHandler(UIError.ShowErrorSnackbar(
            state = recentTracksState,
            fallbackMessage = "Unable to refresh history",
            onAction = localViewModel::refresh
        ))
    }

    Stack(modifier = modifier.fillMaxSize()) {
        if (recentTracksState.isLoading) { LoadingScreen() } else {
            SwipeToRefreshLayout(
                refreshingState = recentTracksState.isRefreshing,
                onRefresh = { localViewModel.refresh() },
                refreshIndicator = { SwipeRefreshProgressIndicator() }
            ) {
                recentTracksState.currentData?.let { list ->
                    HistoryTrackList(
                        tracks = list,
                        onActionClicked = { track, actionType ->
                            selectedTrack.value = track
                            when (actionType) {
                                EDIT -> showEditDialog = true
                                DELETE -> showConfirmDialog = true
                                OPEN -> actionHandler(ListingSelected(track.asLastFmTrack()))
                                SUBMIT -> localViewModel.submitScrobble(track)
                            }
                        },
                        onNowPlayingSelected = { }
                    )
                }
            }
        }
        if (cachedNumber > 0) {
            ExtendedFloatingActionButton(
                text = { Text(text = "$cachedNumber Scrobbles") },
                onClick = { localViewModel.scheduleScrobbleSubmission() },
                icon = { Icon(asset = Icons.Outlined.CloudUpload) },
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)
            )
        }
    }

    if (showEditDialog) {
        TrackEditDialog(onSelect = { track ->
            showEditDialog = false
            track?.let { updatedTrack -> localViewModel.editCachedScrobble(updatedTrack) }
        }, onDismiss = {
            showEditDialog = false
        }, track = selectedTrack.value)
    }
    if (showConfirmDialog) {
        ConfirmDialog(
            title = "Delete Scrobble",
            description = "Are you sure you want to delete the selected scrobble?") { confirmed ->
            if (confirmed && selectedTrack.value != null) {
                selectedTrack.value?.let { localViewModel.deleteScrobble(it) }
            }
            showConfirmDialog = false
        }
    }
}

@Composable
fun HistoryTrackList(
    tracks: List<Scrobble>,
    onActionClicked: (Scrobble, ScrobbleAction) -> Unit,
    onNowPlayingSelected: (Scrobble) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumnFor(items = tracks, modifier = modifier) { track ->
        if (track.isPlaying()) {
            NowPlayingItem(name = track.name, artist = track.artist, onClick = { onNowPlayingSelected(track) })
        } else {
            ScrobbleItem(track = track, onActionClicked = { onActionClicked(track, it) })
        }
    }
}