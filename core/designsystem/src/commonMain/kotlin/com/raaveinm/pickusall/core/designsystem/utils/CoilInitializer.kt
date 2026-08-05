package com.raaveinm.pickusall.core.designsystem.utils

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header

//
// Created by Kirill "Raaveinm" on 5/6/26.
//

@Composable
fun CoilInitializer() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(
                    KtorNetworkFetcherFactory(
                        httpClient = {
                            HttpClient {
                                followRedirects = true
                                defaultRequest {
                                    header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                                    header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                                }
                            }
                        }
                    )
                )
            }
            .build()
    }
}