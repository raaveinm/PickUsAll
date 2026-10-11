package com.raaveinm.picasso.ui.friends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.elements.FriendsMenuDestinations
import com.raaveinm.picasso.ui.friends.elements.NestedFriendsMenu
import com.raaveinm.picasso.ui.friends.screens.FriendContactList
import com.raaveinm.picasso.ui.friends.screens.FriendListScreen
import com.raaveinm.picasso.ui.friends.screens.FriendPendingScreen
import com.raaveinm.picasso.ui.friends.screens.FriendSearchScreen
import com.raaveinm.picasso.ui.navigation.FriendContacts
import com.raaveinm.picasso.ui.navigation.FriendList
import com.raaveinm.picasso.ui.navigation.FriendPending
import com.raaveinm.picasso.ui.navigation.FriendSearch
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.friends_steam_private

internal val ContentWidth = 512.dp

/**
 * The Friends tab: contacts to talk to, search (Steam friends, lookup, outgoing requests),
 * contact management and incoming requests - switched by [NestedFriendsMenu].
 *
 * @param onMessageClick called with the id of the DM to open, creating it first if none exists yet.
 */
@Composable
fun FriendScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    appViewModel: AppViewModel,
    onMessageClick: (Long) -> Unit = {},
    nestedNavController: NavHostController = rememberNavController()
) {
    val backStackEntry by nestedNavController.currentBackStackEntryAsState()
    val friendScreen = backStackEntry?.destination.toFriendsMenu()

    val state by viewModel.friendsUiState.collectAsState()
    val chatState by viewModel.chatsUiState.collectAsState()

    // the social actions on these screens report back through ChatViewModel's warning
    LaunchedEffect(chatState.warningSeq) {
        val warning = chatState.warning ?: return@LaunchedEffect
        appViewModel.postMessage(warning.first, warning.second)
        viewModel.consumeWarning()
    }
    var dismissed by remember { mutableStateOf(false) }

    val friends = remember(state.friends) { state.friends }

    if (friends.isEmpty() && !dismissed) {
        appViewModel.postMessage(WarnLevel.INFO, stringResource(Res.string.friends_steam_private))
        dismissed = true
    }

    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = Dimensions.medium),
        contentAlignment = Alignment.Center
    ) {
        NavHost(
            navController = nestedNavController,
            startDestination = FriendList
        ) {
            composable<FriendList> { // Your friends
                FriendListScreen(
                    modifier = Modifier.sizeIn(maxWidth = ContentWidth).fillMaxSize(),
                    viewModel = viewModel,
                    onMessageClick = onMessageClick
                )
            }
            composable<FriendSearch> { // steam search
                FriendSearchScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onMessageClick = onMessageClick
                )
            }
            composable<FriendContacts> { // associated users (ally / same server strangers / blocked users / friends) and management
                FriendContactList(
                    modifier = Modifier.sizeIn(maxWidth = ContentWidth + Dimensions.extraLarge).fillMaxSize(),
                    viewModel = viewModel
                )
            }
            composable<FriendPending> { // incoming ally requests (outgoing ones are withdrawn from search)
                FriendPendingScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        NestedFriendsMenu(
            modifier = Modifier
                .padding(bottom = Dimensions.paddingAverage, end = Dimensions.small)
                .align(Alignment.BottomEnd),
            selectedMenu = friendScreen,
            onMenuSelected = {
                val route: Any = when (it) {
                    FriendsMenuDestinations.FRIENDLIST -> FriendList
                    FriendsMenuDestinations.SEARCH -> FriendSearch
                    FriendsMenuDestinations.CONTACTS -> FriendContacts
                    FriendsMenuDestinations.PENDING -> FriendPending
                }
                // tabs, not a history: at most the list plus one other screen on the stack
                nestedNavController.navigate(route) {
                    popUpTo(FriendList)
                    launchSingleTop = true
                }
            }
        )
    }
}

/** Which menu entry the nested destination belongs to; the list until the graph has one. */
private fun NavDestination?.toFriendsMenu(): FriendsMenuDestinations = when {
    this == null -> FriendsMenuDestinations.FRIENDLIST
    hasRoute<FriendSearch>() -> FriendsMenuDestinations.SEARCH
    hasRoute<FriendContacts>() -> FriendsMenuDestinations.CONTACTS
    hasRoute<FriendPending>() -> FriendsMenuDestinations.PENDING
    else -> FriendsMenuDestinations.FRIENDLIST
}
