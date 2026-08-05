package com.raaveinm.picasso.data

import com.raaveinm.picasso.AppConfig

//
// Created by Kirill "Raaveinm" on 8/5/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//
class ApiClient {
    private val steamApi: String
        get() = AppConfig.STEAM_API_KEY
}