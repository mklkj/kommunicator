package io.github.mklkj.kommunicator.di

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import io.github.mklkj.kommunicator.data.db.AppDatabase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private val platformModule = module {
    single<SqlDriver> { NativeSqliteDriver(AppDatabase.Schema, "kommunicator.db") }
}

fun initKoin() {
    startKoin {
        modules(commonModule, koinDefaultModule, platformModule)
    }
}

expect val koinDefaultModule: Module
