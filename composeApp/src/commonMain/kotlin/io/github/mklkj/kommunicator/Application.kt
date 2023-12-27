package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.enableCrashlytics

object Application {

    fun initialize() {
        ApplicationPlatform.initialize()
        enableCrashlytics()
    }
}

expect object ApplicationPlatform {
    fun initialize()
}
