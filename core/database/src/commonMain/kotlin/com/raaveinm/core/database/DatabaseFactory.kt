package com.raaveinm.core.database

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DatabaseFactory {
    fun createDatabase(): PicassoDatabase
}

internal const val DB_FILE_NAME = "picasso.db"
