package de.schnettler.scrobbler.components

import androidx.compose.foundation.Box
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.offsetPx
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Surface
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onCommit
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.dp

private val RefreshDistance = 80.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToRefreshLayout(
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    refreshIndicator: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val refreshDistance = with(DensityAmbient.current) { RefreshDistance.toPx() }
    val state = rememberSwipeableState(refreshingState)
    onCommit(refreshingState) {
        state.animateTo(refreshingState)
    }
    // When complete the swipe-to-refresh, kick off the action
    onCommit(state.value) {
        if (state.value) {
            onRefresh()
        }
    }

    Stack(
        modifier = Modifier.swipeable(
            state = state,
            anchors = mapOf(
                -refreshDistance to false,
                refreshDistance to true
            ),
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Vertical
        )
    ) {
        content()
        Box(Modifier.gravity(Alignment.TopCenter).offsetPx(y = state.offset)) {
            if (state.offset.value != -refreshDistance) {
                refreshIndicator()
            }
        }
    }
}

@Composable
fun SwipeRefreshProgressIndicator() {
    Surface(elevation = 10.dp, shape = CircleShape, modifier = Modifier.preferredSize(40.dp)) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.5.dp
        )
    }
}