package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import co.touchlab.kermit.platformLogWriter

object Application {

    @OptIn(ExperimentalKermitApi::class)
    fun initialize() {
        ApplicationPlatform.initialize()
        enableCrashlytics()
        Logger.setLogWriters(buildList {
            if (BuildKonfig.IS_DEBUG) {
                add(platformLogWriter())
            } else add(CrashlyticsLogWriter())
        })
    }
}

expect object ApplicationPlatform {
    fun initialize()
}
