package com.raaveinm.pickusall.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

//
// Created by Kirill "Raaveinm" on 8/4/26.
//

object Shapes {
    val smallShape = 8.dp
    val averageShape = 12.dp

    val roundedAverage = RoundedCornerShape(averageShape)
    val circleShape = RoundedCornerShape(100.dp)

    val incomeChatShape = RoundedCornerShape(0.dp, averageShape, averageShape, averageShape)
    val outcomeShape =  RoundedCornerShape(averageShape, averageShape, 0.dp, averageShape)
}
