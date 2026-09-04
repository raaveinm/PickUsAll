package com.raaveinm.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseFactory {
    actual fun createDatabase(): PicassoDatabase {
        val appDataDir = File(System.getProperty("user.home"), ".picasso").apply { mkdirs() }
        val dbFile = File(appDataDir, DB_FILE_NAME)
        return Room.databaseBuilder<PicassoDatabase>(name = dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
