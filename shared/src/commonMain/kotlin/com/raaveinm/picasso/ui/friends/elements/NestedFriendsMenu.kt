package com.raaveinm.picasso.ui.friends.elements

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/10/2026.
//

@Preview
@Composable
fun NestedFriendsMenu(
    modifier: Modifier = Modifier,
    selectedMenu: FriendsMenuDestinations = FriendsMenuDestinations.FRIENDLIST,
    onMenuSelected: (FriendsMenuDestinations) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.small),
        modifier = modifier.animateContentSize()
    ) {
        FriendsMenuDestinations.entries.forEach {
            if (!isExpanded && selectedMenu != it) return@forEach
            Box(
                Modifier
                    .size(64.dp)
                    .clip(Shapes.circleShape)
                    .background(
                        if (selectedMenu == it) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { if (selectedMenu == it) isExpanded = !isExpanded else onMenuSelected(it) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (it){
                        FriendsMenuDestinations.FRIENDLIST -> Icons.Outlined.WavingHand
                        FriendsMenuDestinations.SEARCH -> Icons.Filled.Block
                        FriendsMenuDestinations.CONTACTS -> Icons.Filled.Search
                        FriendsMenuDestinations.PENDING -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}