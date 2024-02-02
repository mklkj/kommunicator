package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook

actual object ApplicationPlatform {

    actual fun initialize() {
        setCrashlyticsUnhandledExceptionHook()
    }
}
