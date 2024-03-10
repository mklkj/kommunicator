package io.github.mklkj.kommunicator.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.github.mklkj.kommunicator.data.db.AppDatabase
import io.github.mklkj.kommunicator.ui.utils.PlatformInfo
import io.github.mklkj.kommunicator.ui.utils.PlatformInfoIos
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.module

private val platformModule = module {
    single<SqlDriver> { NativeSqliteDriver(AppDatabase.Schema, "kommunicator.db") }
    single<PlatformInfo> { PlatformInfoIos() }
}

@Suppress("unused")
fun initKoin() {
    startKoin {
        commonModule()
        modules(platformModule)
        modules(AppModule.module)
    }
}
