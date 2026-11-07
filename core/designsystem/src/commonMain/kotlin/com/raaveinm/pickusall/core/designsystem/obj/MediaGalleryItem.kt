package com.raaveinm.pickusall.core.designsystem.obj

//
// Created by Kirill "Raaveinm" on 11/07/2026.
//

/** Shared media payload for carousel cards and fullscreen side-window presentations. */
interface MediaGalleryItem {
    data class Image(val url: String) : MediaGalleryItem

    data class Movie(
        val title: String,              // Display name supplied by the media source.
        val thumbnailUrl: String?,      // Poster shown before playback and on adjacent pages.
        val videoUrls: List<String>,    // Alternative encodings of the same movie, in fallback order.
    ) : MediaGalleryItem
}
