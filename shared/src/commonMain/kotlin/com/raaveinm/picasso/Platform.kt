package com.raaveinm.picasso

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform