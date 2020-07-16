package de.schnettler.scrobbler.screens

import androidx.compose.Composable
import androidx.ui.layout.Column
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import de.schnettler.database.models.Artist
import de.schnettler.scrobbler.components.GenericHorizontalListingScroller
import de.schnettler.scrobbler.components.ListingCard

@Preview
@Composable
fun ListingCardPreview() {
        Column {
            val artist = Artist(
                name = "Dreamcatcher",
                url = "Url",
                userPlays = 10,
                plays = 20)
            ListingCard(
                onEntrySelected = {},
                data = artist,
                plays = 10)
            ListingCard(
                onEntrySelected = {},
                data = artist,
                plays = -1)
        }
}

@Preview
@Composable
fun HorizontalListingScrollerPreview() {
    val artists = listOf(
        Artist(
            name = "Dreamcatcher",
            url = "Url",
            userPlays = 10,
            plays = 20
        ),
        Artist(
            name = "All time Low",
            url = "Url",
            userPlays = 10,
            plays = 20
        )
    )
    GenericHorizontalListingScroller(
        items = artists,
        height = 200.dp
    ) { listing ->
        ListingCard(data = listing, onEntrySelected = {})
    }
}