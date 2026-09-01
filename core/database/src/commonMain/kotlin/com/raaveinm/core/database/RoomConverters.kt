package com.raaveinm.core.database

//
// Created by Kirill "Raaveinm" on 9/1/26.
//

import androidx.room3.ColumnTypeConverter
import com.raaveinm.core.model.game.LibraryOrder

class RoomConverters {
    @ColumnTypeConverter
    fun fromLibraryOrder(value: LibraryOrder): String = value.name

    @ColumnTypeConverter
    fun toLibraryOrder(value: String): LibraryOrder = LibraryOrder.valueOf(value)
}
