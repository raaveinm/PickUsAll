package com.raaveinm.core.datastore

import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.auth.AuthTokenStore
import com.raaveinm.core.datastore.auth.createAuthDataStore
import org.koin.dsl.module

//
// Created by Kirill "Raaveinm" on 10/1/26.
//


fun authDataStoreModule(factory: AuthDataStoreFactory) = module {
    single { createAuthDataStore { factory.authDataStorePath() } }
    single { AuthTokenStore(get()) }
}
