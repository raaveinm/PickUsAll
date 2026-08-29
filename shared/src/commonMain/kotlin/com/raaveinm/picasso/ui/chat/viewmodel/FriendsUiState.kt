package com.raaveinm.picasso.ui.chat.viewmodel

import com.raaveinm.core.model.user.User

data class FriendsUiState(
    val friends: List<User> = emptyList()
)
