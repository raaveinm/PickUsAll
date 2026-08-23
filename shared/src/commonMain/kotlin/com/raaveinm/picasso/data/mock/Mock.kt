package com.raaveinm.picasso.data.mock

import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.core.model.game.CommunityContent
import com.raaveinm.core.model.user.OwnedGame
import com.raaveinm.core.model.user.User

object Mock {
    val libraryList: List<OwnedGame> = listOf(
        OwnedGame(
            appId = 1808500,
            name = "Arc Raiders",
            playtime2Weeks = 407,
            playtimeForever = 16877,
            imgIconUrl = "c284e73b6f3321864805d66f99924a0da9f0b219",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 2935,
            playtimeMacForever = 0,
            playtimeLinuxForever = 13941,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1787003896,
            playtimeDisconnected = 37,
        ),
        OwnedGame(
            appId = 1091500,
            name = "Cyberpunk 2077",
            playtime2Weeks = 310,
            playtimeForever = 24634,
            imgIconUrl = "6897c3848f3e0350d512f59d5bae174a1e3739f9",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 21363,
            playtimeMacForever = 0,
            playtimeLinuxForever = 3270,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1786658136,
            playtimeDisconnected = 0,
        ),
        OwnedGame(
            appId = 2073850,
            name = "THE FINALS",
            playtime2Weeks = 499,
            playtimeForever = 4674,
            imgIconUrl = "9532db560dca3b4982f4af3f5981b6b2ce2a6909",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 460,
            playtimeMacForever = 0,
            playtimeLinuxForever = 4213,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1786914018,
            playtimeDisconnected = 0,
        ),
        OwnedGame(
            appId = 1601580,
            name = "Frostpunk 2",
            playtimeForever = 417,
            imgIconUrl = "5e66161686d4e2503a8a42aab9e8bc1c46c68fc1",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 384,
            playtimeMacForever = 11,
            playtimeLinuxForever = 20,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1767214834,
            playtimeDisconnected = 0,
            playtime2Weeks = 0
        ),
        OwnedGame(
            appId = 264710,
            name = "Subnautica",
            playtimeForever = 5522,
            imgIconUrl = "8a14ceef6e230330a916d7a6324b8c52d464d569",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 5455,
            playtimeMacForever = 51,
            playtimeLinuxForever = 15,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1776366237,
            playtimeDisconnected = 0,
            playtime2Weeks = 0,
        ),
        OwnedGame(
            appId = 848450,
            name = "Subnautica: Below Zero",
            playtimeForever = 5248,
            imgIconUrl = "eae16718327a78d87ac7ae61db96729926c97a16",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 5234,
            playtimeMacForever = 0,
            playtimeLinuxForever = 14,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1774177159,
            playtimeDisconnected = 0,
            playtime2Weeks = 0,
        ),
        OwnedGame(
            appId = 1293830,
            name = "Forza Horizon 4",
            playtimeForever = 9248,
            imgIconUrl = "7c993f9089d54fe2767efec47b75a7009cdd632e",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 9241,
            playtimeMacForever = 0,
            playtimeLinuxForever = 6,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1783247109,
            playtimeDisconnected = 5,
            playtime2Weeks = 0,
        ),
        OwnedGame(
            appId = 1911610,
            name = "Windblown",
            playtimeForever = 414,
            imgIconUrl = "cd297be670a9fb510af69fb31cab14a60712bd67",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 206,
            playtimeMacForever = 0,
            playtimeLinuxForever = 208,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1783382995,
            playtimeDisconnected = 0,
            playtime2Weeks = 0,
        ),
        OwnedGame(
            appId = 730,
            name = "Counter-Strike 2",
            playtimeForever = 46350,
            imgIconUrl = "8dbc71957312bbd3baea65848b545be9eae2a355",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 42211,
            playtimeMacForever = 70,
            playtimeLinuxForever = 3949,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1787001177,
            playtimeDisconnected = 3,
            playtime2Weeks = 203,
        ),
        OwnedGame(
            appId = 359550,
            name = "Tom Clancy's Rainbow Six Siege",
            playtimeForever = 66613,
            imgIconUrl = "624745d333ac54aedb1ee911013e2edb7722550e",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 66578,
            playtimeMacForever = 0,
            playtimeLinuxForever = 34,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1780425827,
            playtimeDisconnected = 30,
            playtime2Weeks = 0,
        ),
        OwnedGame(
            appId = 620980,
            name = "Beat Saber",
            playtimeForever = 13786,
            imgIconUrl = "d2922f271fca9579ecdcc07408cee13d87ca4148",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 13786,
            playtimeMacForever = 0,
            playtimeLinuxForever = 0,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1786964511,
            playtimeDisconnected = 56,
            playtime2Weeks = 80,
        ),
    )

    val gameListCommunityContent: List<CommunityContent> = listOf(
        CommunityContent(
            ownedGame = OwnedGame(
                appId = 620980,
                name = "Beat Saber",
                playtimeForever = 13786,
                imgIconUrl = "d2922f271fca9579ecdcc07408cee13d87ca4148",
                hasCommunityVisibleStats = true,
                playtimeWindowsForever = 13786,
                playtimeMacForever = 0,
                playtimeLinuxForever = 0,
                playtimeDeckForever = 0,
                rtimeLastPlayed = 1786964511,
                playtimeDisconnected = 56,
                playtime2Weeks = 80,
            ),
            imageUrl = listOf(
                "ss_542d092f42c779c866167bec05c1da488bcd91f8",
                "ss_b65444cc4513f34bd41fa6b0fe96cf11d94fea8d",
                "ss_7df971fd7781d69dc455b15a400a6973ed7d3f36"
            ),
            inLibrary = true,
            gameTags = listOf("asdsad")
        )
    )

    val mostPlayedGames: List<OwnedGame> = listOf(
        OwnedGame(
            appId = 1808500,
            name = "Arc Raiders",
            playtime2Weeks = 407,
            playtimeForever = 16877,
            imgIconUrl = "c284e73b6f3321864805d66f99924a0da9f0b219",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 2935,
            playtimeMacForever = 0,
            playtimeLinuxForever = 13941,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1787003896,
            playtimeDisconnected = 37,
        ),
        OwnedGame(
            appId = 1091500,
            name = "Cyberpunk 2077",
            playtime2Weeks = 310,
            playtimeForever = 24634,
            imgIconUrl = "6897c3848f3e0350d512f59d5bae174a1e3739f9",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 21363,
            playtimeMacForever = 0,
            playtimeLinuxForever = 3270,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1786658136,
            playtimeDisconnected = 0,
        ),
        OwnedGame(
            appId = 2073850,
            name = "THE FINALS",
            playtime2Weeks = 499,
            playtimeForever = 4674,
            imgIconUrl = "9532db560dca3b4982f4af3f5981b6b2ce2a6909",
            hasCommunityVisibleStats = true,
            playtimeWindowsForever = 460,
            playtimeMacForever = 0,
            playtimeLinuxForever = 4213,
            playtimeDeckForever = 0,
            rtimeLastPlayed = 1786914018,
            playtimeDisconnected = 0,
        ),
    )

    val user_1 = User(
        steamId = 1,
        communityVisibilityState = 3,
        personaName = "raaveinm",
        realName = "Kira \"Raaveinm\"",
        commentPermission = true,
        profileUrl = "https://steamcommunity.com/id/raaveinm/",
        avatar = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79.jpg",
        avatarMedium = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_medium.jpg",
        avatarFull = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_full.jpg",
        avatarHash = "b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79",
        personaState = 1
    )
    val user_2 = User(
        steamId = 2,
        communityVisibilityState = 3,
        personaName = "Nick\uD83D\uDC3E",
        commentPermission = true,
        profileUrl = "https://steamcommunity.com/profiles/76561198325678334/",
        avatar = "https://avatars.steamstatic.com/6f4944ce1cd6bc9848125d6bc82d380853df9253.jpg",
        avatarMedium = "https://avatars.steamstatic.com/6f4944ce1cd6bc9848125d6bc82d380853df9253_medium.jpg",
        avatarFull = "https://avatars.steamstatic.com/6f4944ce1cd6bc9848125d6bc82d380853df9253_full.jpg",
        avatarHash = "",
        personaState = 1
    )
    val user_3 = User(
        steamId = 3,
        communityVisibilityState = 3,
        personaName = "koul",
        commentPermission = false,
        profileUrl = "https://www.furaffinity.net/user/koul/",
        avatar = "https://d.furaffinity.net/art/koul/1634771898/1634771898.koul_storm_shep.png",
        avatarMedium = "https://d.furaffinity.net/art/koul/1634771898/1634771898.koul_storm_shep.png",
        avatarFull = "https://d.furaffinity.net/art/koul/1634771898/1634771898.koul_storm_shep.png",
        avatarHash = "",
        personaState = 1
    )
    val user_warn = User(
        steamId = 4,
        communityVisibilityState = 1,
        personaName = "WARNING",
        commentPermission = false,
        profileUrl = "https://www.furaffinity.net/user/koul/",
        avatar = "https://en.wikipedia.org/wiki/Biological_hazard#/media/File:Air_pollution3.jpg",
        avatarMedium = "https://en.wikipedia.org/wiki/Biological_hazard#/media/File:Air_pollution3.jpg",
        avatarFull = "https://en.wikipedia.org/wiki/Biological_hazard#/media/File:Air_pollution3.jpg",
        avatarHash = "",
        personaState = 1
    )

    val userList: List<User> = listOf(user_1, user_2, user_3, user_warn)

    val chatList: List<Chat> = listOf(
        Chat(
            id = user_warn.steamId,
            chatTitle = user_warn,
            lastMessage = "If you see this chat - something is wrong"
        ),
        Chat(
            id = user_1.steamId,
            chatTitle = user_1,
            lastMessage = "Mornin' <3"
        ),
        Chat(
            id = user_2.steamId,
            chatTitle = user_2,
            lastMessage = null
        ),
        Chat(
            id = user_3.steamId,
            chatTitle = user_3,
            lastMessage = "The lunatic is on the grass\n" +
                    "Remembering games\n" +
                    "And daisy chains and laughs\n" +
                    "Got to keep the loonies on the path\n" +
                    "The lunatic is in the hall\n" +
                    "The lunatics are in my hall\n" +
                    "The paper holds their folded faces to the floor \n" +
                    "And every day the paper boy brings more",
        )
    )

    val paletteList: List<Palette> = listOf(
        Palette(
            id = 1001,
            name = "Raid Night",
            members = listOf(user_1, user_2, user_3),
            lastMessage = "koul: The lunatic is on the grass"
        )
    )

    val conversationList: List<Conversation> = chatList + paletteList
}
