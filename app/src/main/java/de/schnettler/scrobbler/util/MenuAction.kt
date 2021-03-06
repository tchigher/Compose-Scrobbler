package de.schnettler.scrobbler.util

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.ui.graphics.vector.VectorAsset
import de.schnettler.scrobbler.R

sealed class MenuAction(@StringRes val label: Int, val icon: VectorAsset) {
    object Period : MenuAction(R.string.ic_period, Icons.Rounded.Event)
    class OpenInBrowser(val url: String) : MenuAction(R.string.ic_open_in, Icons.Rounded.OpenInBrowser)
}