package com.raaveinm.picasso.ui.chat.viewmodel

import com.raaveinm.core.model.user.User

data class FriendsUiState(
    val friends: List<User> = emptyList(),
    val isRefreshing: Boolean = false
)


/**
 * The Artist search on the Friends tab. [result] is the cached profile [query] resolved to;
 * [notFound] is set only after a finished lookup that matched nobody.
 */
data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val result: User? = null,
    val notFound: Boolean = false
)
