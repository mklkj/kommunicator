package io.github.mklkj.kommunicator.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.mklkj.kommunicator.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

private val platformModule = module {
    single<SqlDriver> { AndroidSqliteDriver(AppDatabase.Schema, get(), "kommunicator.db") }
}

fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(commonModule, defaultModule, platformModule)
    }
}
