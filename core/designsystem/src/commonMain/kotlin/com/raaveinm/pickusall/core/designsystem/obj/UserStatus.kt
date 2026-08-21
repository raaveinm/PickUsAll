package com.raaveinm.pickusall.core.designsystem.obj

@Suppress("unused")
object UserStatus {
    fun getOnlineStatus(onlineState: Int): String =
        when (onlineState) {
            0 -> offlineStatusList.random()
            1 -> onlineStatusList.random()
            2 -> busyStatusList.random()
            3 -> awayStatusList.random()
            4 -> snoozeStatusList.random()
            5 -> onlineStatusList.random()
            6 -> onlineStatusList.random()
            else -> offlineStatusList.random()
        }
    private val onlineStatusList = listOf(
        "preparing canvas",
        "mixing colors",
        "stretching a new canvas",
        "charting new constellations",
        "sharpening pencils"
    )

    private val offlineStatusList = listOf(
        "sniffing for inspiration",
        "drifting through the void",
        "cleaning brushes",
        "gone dark side of the moon",
        "let the paint dry",
        "orbiting quietly"
    )

    private val busyStatusList = listOf("busy")
    private val awayStatusList = listOf("away")
    private val snoozeStatusList = listOf("snooze")

    fun getPrivateProfileStatus(): String = privateProfileStatusList.random()

    private val privateProfileStatusList = listOf(
        "this profile is private :(",
        "Artist wanna stay in the dark",
        "Is it Banksy?"
    )
}