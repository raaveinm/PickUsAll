package com.raaveinm.picasso.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.responses.ProfileItem
import com.raaveinm.picasso.data.ApiClient
import com.raaveinm.pickusall.core.designsystem.utils.AnimatedBackground
import com.raaveinm.pickusall.core.designsystem.utils.ImageUtility
import kotlinx.coroutines.CancellationException

//
// Created by Kirill "Raaveinm" on 10/26/2026.
//

private const val STEAM_COMMUNITY_CDN = "https://cdn.fastly.steamstatic.com/steamcommunity/public/images/"

private class ProfileBackground(val videoUrl: String?, val imageUrl: String?)

private fun String.toCdnUrl(): String = if (startsWith("http")) this else STEAM_COMMUNITY_CDN + this

private fun ProfileItem.toProfileBackground(): ProfileBackground = ProfileBackground(
    videoUrl = (movieMp4 ?: movieWebm)?.toCdnUrl(),
    imageUrl = imageLarge?.toCdnUrl(),
)

@Composable
fun SteamPFB(backgroundOwner: Long?, apiClient: ApiClient) {
    Box(Modifier.fillMaxSize()) {
        val background by produceState<ProfileBackground?>(null, backgroundOwner) {
            value = backgroundOwner?.let { owner ->
                try {
                    apiClient.getProfileBackground(owner)?.toProfileBackground()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    println("ChatScreen: profile background failed: $e"); null
                }
            }
        }
        background?.let { item ->
            Box(Modifier.fillMaxSize().zIndex(-1f)) {
                if (item.videoUrl != null) {
                    AnimatedBackground(
                        modifier = Modifier.fillMaxSize(),
                        videoUrl = item.videoUrl,
                        posterUrl = item.imageUrl,
                    )
                } else if (item.imageUrl != null) {
                    ImageUtility(
                        modifier = Modifier.fillMaxSize(),
                        imageUrl = item.imageUrl,
                        blurPadding = 0.dp,
                    )
                }
            }
        }
    }
}