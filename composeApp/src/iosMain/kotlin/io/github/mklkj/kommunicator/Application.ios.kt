package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import cocoapods.FirebaseCore.FIRApp

actual object ApplicationPlatform {

    actual fun initialize() {
        FIRApp.initialize()
        setCrashlyticsUnhandledExceptionHook()
    }
}
