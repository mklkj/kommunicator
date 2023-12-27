package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.enableCrashlytics

object Application {

    fun initialize() {
        enableCrashlytics()
        ApplicationPlatform.initialize()
    }
}

expect object ApplicationPlatform {
    fun initialize()
}
