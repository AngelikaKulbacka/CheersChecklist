package com.example.cheerschecklist

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

val appModule = module {
    single<AppDatabase> { getRoomDatabase(getDatabaseBuilder()) }
    single<TastedDrinkDao> { get<AppDatabase>().tastedDrinkDao() }
    single<TastedDrinkRepository> { RoomTastedDrinkRepository(get()) }
}

fun initKoin() {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        modules(appModule)
    }
}
