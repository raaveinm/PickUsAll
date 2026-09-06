package com.raaveinm.core.model.responses

//
// Created by Kirill "Raaveinm" on 9/4/26.
//

import com.raaveinm.core.model.user.Friend
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFriendListResponse(
    @SerialName("friendslist")
    val friendsList: FriendListBody? = null
)

@Serializable
data class FriendListBody(
    val friends: List<Friend> = emptyList()
)
