package com.raaveinm.picasso.data.mock

import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.core.model.game.Game
import com.raaveinm.core.model.game.GameApiMovies
import com.raaveinm.core.model.game.GameApiPlatforms
import com.raaveinm.core.model.game.GameApiPriceOverview
import com.raaveinm.core.model.game.GameApiReleaseDate
import com.raaveinm.core.model.game.GameApiScreenshot
import com.raaveinm.core.model.user.OwnedGame
import com.raaveinm.core.model.user.User

object Mock {

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
        commentPermission = 1,
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
        commentPermission = 1,
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
        commentPermission = 0,
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
        commentPermission = 0,
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

    val game: Game = Game(
        steamAppId = 1808500,
        name = "ARC Raiders",
        type = "game",
        shortDescription = "ARC Raiders is a multiplayer extraction adventure, set in a lethal future" +
                " earth, ravaged by a mysterious mechanized threat known as ARC",
        aboutGame = "<h2 class=\"bb_tag\" >SCAVENGE, SURVIVE, THRIVE </h2><p class=\"bb_paragraph\" >In ARC Raiders, gameplay flows between the surface ruled by lethal machines, and the vibrant underground society of Speranza. Craft, repair, and upgrade your gear in the safety of your own workshop, before venturing topside to scavenge the remnants of a devastated but beautiful world. Play solo or in parties up to three, navigating the constant threat of ARC's machines and the unpredictable choices of fellow survivors. In the end, only you decide what kind of Raider you are - and how far you’ll go to prevail.<br><span class=\"bb_img_ctn\"><video class=\"bb_img\" autoplay muted loop playsinline crossorigin=\"anonymous\" poster=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/f22cb839ef45f9106db05672412463f3.poster.avif?t=1793353805\" width=780 height=252 ><source src=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/f22cb839ef45f9106db05672412463f3.webm?t=1793353805\" type=\"video/webm; codecs=vp9\"></video></span></p><p class=\"bb_paragraph\" ></p><h2 class=\"bb_tag\" >EXPLORE AN IMMERSIVE WORLD</h2><p class=\"bb_paragraph\" >Explore four distinct maps at launch, with more to be revealed as the underground society evolves and expands its reach. Each destination carries the weight of a world twice-destroyed, and the scars of conflicts both old and new. Sift through the remnants for valuable loot, and piece together the past as it's slowly reclaimed by nature. Evolving map conditions ensure that no two runs are the same, with varying weather, enemies, and mechanics adding unpredictability and danger. <br><span class=\"bb_img_ctn\"><video class=\"bb_img\" autoplay muted loop playsinline crossorigin=\"anonymous\" poster=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/138bc82b1d7cd44c7a8c1b40ff9351aa.poster.avif?t=1793353805\" width=780 height=252 ><source src=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/138bc82b1d7cd44c7a8c1b40ff9351aa.webm?t=1793353805\" type=\"video/webm; codecs=vp9\"></video></span></p><p class=\"bb_paragraph\" ></p><h2 class=\"bb_tag\" >STAKE YOUR CLAIM</h2><p class=\"bb_paragraph\" >In a society built on boldness and bravado, it is up to you to stake your claim as a Raider. The loot you scavenge can be sold for coin or crafted into all-new gear, allowing you take on increasingly lethal dangers head-on. Through both victories and hardships, you'll gain valuable experience; unlocking varied skills that enable all-new ways of play. In addition, you'll complete quests for Traders with differing motives and agendas; uncovering both the friction and comradery of a community under constant threat of collapse.</p><p class=\"bb_paragraph\" ><span class=\"bb_img_ctn\"><video class=\"bb_img\" autoplay muted loop playsinline crossorigin=\"anonymous\" poster=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/cb5b1a7db7fc18a2443892b8f437a2e2.poster.avif?t=1793353805\" width=780 height=252 ><source src=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/cb5b1a7db7fc18a2443892b8f437a2e2.webm?t=1793353805\" type=\"video/webm; codecs=vp9\"></video></span></p><p class=\"bb_paragraph\" ></p><h2 class=\"bb_tag\" >BEWARE THE MACHINES</h2><p class=\"bb_paragraph\" >Lethal machines known as ARC rule the surface, ranging from unrelenting drone swarms to mechanical giants that obliterate everything in their path. Their origins remain a mystery, but their ever-present danger is felt with every step you take. Each machine comes with distinct strengths and tactics, forcing you to pinpoint their weak spots and constantly think on your feet. And remember: the noise of battle carries. Other Raiders may be listening; eager to claim what you leave behind.</p><h2 class=\"bb_tag\" style=\"text-align: center\"><span class=\"bb_img_ctn\"><video class=\"bb_img\" autoplay muted loop playsinline crossorigin=\"anonymous\" poster=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/bc2b5cb59b0bc6eb42ddc2ff26098545.poster.avif?t=1793353805\" width=780 height=252 ><source src=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/bc2b5cb59b0bc6eb42ddc2ff26098545.webm?t=1793353805\" type=\"video/webm; codecs=vp9\"></video></span></h2><h2 class=\"bb_tag\" >FORGE YOUR OWN PATH</h2><p class=\"bb_paragraph\" >Raiders survive by kitbashing scavenged materials; using long-lost tech and looted ARC parts to craft weapons, gadgets, and gear. Upgrade your workshop stations and learn blueprints to craft even more advanced items, or improvise quick fixes in the field to get yourself out of a bind. As you make a name for yourself, you'll be able to measure your skill against other Raiders by taking on Trials; rising through the leaderboards to earn valuable rewards.</p><p class=\"bb_paragraph\" ></p><p class=\"bb_paragraph\" ><span class=\"bb_img_ctn\"><video class=\"bb_img\" autoplay muted loop playsinline crossorigin=\"anonymous\" poster=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/3ed7c3a1bf71024f7b8938b1b10711f9.poster.avif?t=1793353805\" width=780 height=252 ><source src=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/3ed7c3a1bf71024f7b8938b1b10711f9.webm?t=1793353805\" type=\"video/webm; codecs=vp9\"></video></span></p><h2 class=\"bb_tag\" >QUESTS</h2><p class=\"bb_paragraph\" >In Speranza, everyone has an agenda, and the Traders are no different. They’ll send you topside on missions in exchange for rewards, slowly revealing more about who they are and what they want for Speranza’s future. Complete quests to earn gear, crafting materials, and XP to level up your Raider and unlock new skill points.</p><p class=\"bb_paragraph\" ><span class=\"bb_img_ctn\"><video class=\"bb_img\" autoplay muted loop playsinline crossorigin=\"anonymous\" poster=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/b85286f667febcbea2f732aa222f29ce.poster.avif?t=1793353805\" width=780 height=252 ><source src=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/b85286f667febcbea2f732aa222f29ce.webm?t=1793353805\" type=\"video/webm; codecs=vp9\"></video></span></p><h2 class=\"bb_tag\" >SKILL TREE</h2><p class=\"bb_paragraph\" >The ARC Raiders skill tree branches into three paths: Survival, Mobility, and Conditioning. Spend your points to shape your playstyle. Loot faster and move quieter with Survival skills. Outmaneuver your opponents and threats with Mobility. And enhance your Strength and Stamina with Conditioning. Choose how you grow and what your Raider brings to the battle. </p><p class=\"bb_paragraph\" ><span class=\"bb_img_ctn\"><video class=\"bb_img\" autoplay muted loop playsinline crossorigin=\"anonymous\" poster=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/b20e3d75010301520237f9b904105279.poster.avif?t=1793353805\" width=780 height=252 ><source src=\"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/extras/b20e3d75010301520237f9b904105279.webm?t=1793353805\" type=\"video/webm; codecs=vp9\"></video></span></p><h2 class=\"bb_tag\" >BUILD YOUR ARSENAL</h2><p class=\"bb_paragraph\" >Whether you’re preparing to take down specific ARC enemies or fend off rival Raiders, there's a choice of weaponry for every fight. Cater to your style with a range of firearms including SMGs, rifles and shotguns, as well as more advanced options such as railguns and energy weapons. Grenades, traps, ziplines and deployables offer the tactical depth needed to outsmart your enemies, while augments allow you to tailor your loadout to your preferred playstyle; unlocking dedicated inventory slots and additional gameplay perks.</p><p class=\"bb_paragraph\" style=\"text-align: center\"> </p>",
        detailedDescription = "`<h1>ARC Raiders Deluxe Edition</h1><p><p class=\"bb_paragraph\" ><span class=\"bb_img_ctn\"><img class=\"bb_img\" src=\"https://sha…locking dedicated inventory slots and additional gameplay perks.</p><p class=\"bb_paragraph\" style=\"text-align: center\"> </p>`",
        supportedLanguages = "English<strong>*</strong>, French, Italian, German, Spanish - Spain, Japanese, Korean, Polish, Portuguese - Brazil, Russian, Simplified Chinese, Traditional Chinese, Turkish<br><strong>*</strong>languages with full audio support",
        headerImage = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/41ee6c5c81a5797132879892586465b917acc563/header_alt_assets_9.jpg?t=1793353805",
        website = "https://arcraiders.com/",
        platforms = GameApiPlatforms(windows = true, linux = true),
        priceOverview = GameApiPriceOverview(
            currency = "UAH",
            initial = 1390,
            final = 1390,
            discountPercent = 0,
            initialFormatted = "",
            finalFormatted = "UAH 1390"
        ),
        releaseDate = GameApiReleaseDate(comingSoon = false, date = "30 Oct, 2025"),
        screenshots = listOf(
            GameApiScreenshot(id = 0, pathThumbnail = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/1763664f3ea80080867eafa751685b7feec950c8/ss_1763664f3ea80080867eafa751685b7feec950c8.600x338.jpg?t=1793353805", pathFull = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/1763664f3ea80080867eafa751685b7feec950c8/ss_1763664f3ea80080867eafa751685b7feec950c8.1920x1080.jpg?t=1793353805"),
            GameApiScreenshot(id = 1, pathThumbnail = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/5188aa2b07920afe9753409ea9fe3d1324cfc294/ss_5188aa2b07920afe9753409ea9fe3d1324cfc294.600x338.jpg?t=1793353805", pathFull = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/5188aa2b07920afe9753409ea9fe3d1324cfc294/ss_5188aa2b07920afe9753409ea9fe3d1324cfc294.1920x1080.jpg?t=1793353805")
        ),
        dlc = listOf(5201760, 3948510, 4920260, 4210150, 4424920, 4741040),
        controllerSupport = "full",
        reviews = "86 – Metacritic<br>",
        background = "https://store.akamai.steamstatic.com/images/storepagebackground/app/1808500?t=1793353805",
        backgroundRaw = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1808500/3e3243b88cad554d4ddd237620193ac0c3cae5b3/page_bg_raw.jpg?t=1793353805",
        movies = listOf(
            GameApiMovies(id = 257230049, name = "ABC Trailer", thumbnail = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/257230049/5e5ff0f6856abe9b4a8200dfae19d6ec5ff6e67d/movie_600x337.jpg?t=1764583045", dashAv1 = "https://video.akamai.steamstatic.com/store_trailers/1808500/1688567022/8ecb330fb51867a492cf9436054ef4a9ed45a3af/1762957801/dash_av1.mpd?t=1764583045", dashH264 = "https://video.akamai.steamstatic.com/store_trailers/1808500/1688567022/8ecb330fb51867a492cf9436054ef4a9ed45a3af/1762957801/dash_h264.mpd?t=1764583045", hlsH264 = "https://video.akamai.steamstatic.com/store_trailers/1808500/1688567022/8ecb330fb51867a492cf9436054ef4a9ed45a3af/1762957801/hls_264_master.m3u8?t=1764583045", highlight = true)
        )
    )
}
