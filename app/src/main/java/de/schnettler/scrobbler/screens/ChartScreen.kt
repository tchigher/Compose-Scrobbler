package de.schnettler.scrobbler.screens

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.onActive
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import de.schnettler.scrobbler.UIAction
import de.schnettler.scrobbler.UIAction.ListingSelected
import de.schnettler.scrobbler.UIError
import de.schnettler.scrobbler.components.CustomDivider
import de.schnettler.scrobbler.components.IndexListIconBackground
import de.schnettler.scrobbler.components.LoadingScreen
import de.schnettler.scrobbler.components.SwipeRefreshProgressIndicator
import de.schnettler.scrobbler.components.SwipeToRefreshLayout
import de.schnettler.scrobbler.util.abbreviate
import de.schnettler.scrobbler.viewmodels.ChartsViewModel

@Composable
fun ChartScreen(
    model: ChartsViewModel,
    actionHandler: (UIAction) -> Unit,
    errorHandler: @Composable (UIError) -> Unit,
    modifier: Modifier = Modifier,
) {
    onActive { model.startStream() }
    val chartState by model.state.collectAsState()

    if (chartState.isError) {
        errorHandler(UIError.ShowErrorSnackbar(
            state = chartState,
            fallbackMessage = "Unable to refresh charts",
            onAction = model::refresh
        ))
    }

    if (chartState.isLoading) { LoadingScreen() } else {
        SwipeToRefreshLayout(
            refreshingState = chartState.isRefreshing,
            onRefresh = { model.refresh() },
            refreshIndicator = { SwipeRefreshProgressIndicator() }
        ) {
            chartState.currentData?.let { charts ->
                LazyColumnForIndexed(items = charts, modifier) { index, (entry, artist) ->
                    ChartListItem(artist.name, entry.count, index) { actionHandler(ListingSelected(artist)) }
                    CustomDivider()
                }
            }
        }
    }
}

@Composable
private fun ChartListItem(name: String, listener: Long, index: Int, onClicked: () -> Unit) {
    ListItem(
        text = { Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        secondaryText = {
            Text(
                text = "${listener.abbreviate()} Listener", maxLines = 1, overflow = TextOverflow
                    .Ellipsis
            )
        },
        icon = { IndexListIconBackground(index = index) },
        modifier = Modifier.clickable(onClick = { onClicked() })
    )
}