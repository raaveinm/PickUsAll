package com.raaveinm.core.database

import org.koin.dsl.module

// DatabaseFactory's constructor differs per platform (Android needs a Context, others
// don't), so it can't be built from common code - each platform entry point builds its
// own DatabaseFactory and passes it in here to get a Koin module with the DAOs.
fun databaseModule(factory: DatabaseFactory) = module {
    single { factory.createDatabase() }
    single { get<PicassoDatabase>().getChatDao() }
    single { get<PicassoDatabase>().getGameDao() }
    single { get<PicassoDatabase>().getServerDao() }
    single { get<PicassoDatabase>().getUserDao() }
}
