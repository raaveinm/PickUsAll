package com.raaveinm.picasso

import android.app.Application
import com.raaveinm.core.database.DatabaseFactory
import com.raaveinm.core.database.databaseModule
import com.raaveinm.picasso.di.initKoin
import com.raaveinm.picasso.ui.actions.ClipboardHelper
import org.koin.android.ext.koin.androidContext

class PicassoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ClipboardHelper.init(this)
        initKoin {
            androidContext(this@PicassoApplication)
            modules(databaseModule(DatabaseFactory(this@PicassoApplication)))
        }
    }
}
