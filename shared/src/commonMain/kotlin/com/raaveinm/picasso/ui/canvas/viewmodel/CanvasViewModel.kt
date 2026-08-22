package com.raaveinm.picasso.ui.canvas.viewmodel

import androidx.lifecycle.ViewModel
import com.raaveinm.picasso.data.mock.Mock

class CanvasViewModel : ViewModel() {
    val libraryList = Mock.libraryList
    val gameListCommunityContent = Mock.gameListCommunityContent
}