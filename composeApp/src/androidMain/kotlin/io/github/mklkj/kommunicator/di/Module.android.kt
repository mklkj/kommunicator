package io.github.mklkj.kommunicator.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.mklkj.kommunicator.data.db.AppDatabase
import io.github.mklkj.kommunicator.ui.utils.PlatformInfo
import io.github.mklkj.kommunicator.utils.PlatformInfoAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.module

private val platformModule = module {
    single<SqlDriver> { AndroidSqliteDriver(AppDatabase.Schema, get(), "kommunicator.db") }
    single<PlatformInfo> { PlatformInfoAndroid(get()) }
}

fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        androidLogger()
        commonModule()
        modules(platformModule)
        modules(AppModule.module)
    }
}
