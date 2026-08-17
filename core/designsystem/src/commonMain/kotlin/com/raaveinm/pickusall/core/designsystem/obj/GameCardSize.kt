package com.raaveinm.pickusall.core.designsystem.obj

@Suppress("unused")
object GameCardSize {
    ///////////////////////////////////////////////
    // Base CDN URLs (For Reference)
    ///////////////////////////////////////////////
    // Store Assets: https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$gameId/
    // Icon Assets:  https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/$gameId/$fileName.1920x1080.jpg

    ///////////////////////////////////////////////
    // Library Preview
    ///////////////////////////////////////////////
    const val LIBRARY_600_X_900 = "library_600x900.jpg"         // Cover
    const val LIBRARY_600_X_900_2X = "library_600x900_2x.jpg"   // Cover 2x quality
    const val LIBRARY_300_X_450 = "library_300x450.jpg"         // sniff sniff

    const val LIBRARY_HERO = "library_hero.jpg"                 // 21x9 Poster
    const val LIBRARY_HERO_2X = "library_hero_2x.jpg"           // 21x9 Poster 4k
    const val LIBRARY_LOGO = "logo.png"                         // Transparent PNG

    ///////////////////////////////////////////////
    // Banners
    ///////////////////////////////////////////////
    const val HEADER_IMAGE = "header.jpg"                       // store preview
    const val CAPSULE_231_X_87 = "capsule_231x87.jpg"           // store preview /2
    const val CAPSULE_616_X_353 = "capsule_616x353.jpg"         // store preview big image (diff)

    ///////////////////////////////////////////////
    // Backgrounds
    ///////////////////////////////////////////////
    const val BACKGROUND_PAGE = "page_bg_generated_v6b.jpg"     // smooth ambient image 16x9

    ///////////////////////////////////////////////
    // Screenshots & Community
    ///////////////////////////////////////////////
    fun getScreenshotFull(filename: String): String =           // Community / Game Preview
        "$filename.1920x1080.jpg"

    fun getScreenShotThumbnails(filename: String): String =     // God knows
        "$filename.600x338.jpg"

    ///////////////////////////////////////////////
    // Library Preview
    ///////////////////////////////////////////////
    fun getIcon(iconHash: String, type: ImageType = ImageType.JPG): String =
        "$iconHash.$type"

    enum class ImageType(val extension: String) {
        JPG("jpg"),
        PNG("png");

        override fun toString(): String = extension
    }
}