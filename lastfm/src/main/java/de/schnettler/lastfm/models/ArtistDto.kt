package de.schnettler.lastfm.models

data class ArtistDto(
    override val name: String,
    override val mbid: String,
    override val url: String,
    val playcount: Long,
    val listeners: Long?,
    val streamable: String
): ListingDto(name, mbid, url)