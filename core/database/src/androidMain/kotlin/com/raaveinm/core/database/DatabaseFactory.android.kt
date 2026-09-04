package com.raaveinm.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseFactory(private val context: Context) {
    actual fun createDatabase(): PicassoDatabase {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(DB_FILE_NAME)
        return Room.databaseBuilder<PicassoDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }
}
