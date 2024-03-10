package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration

actual object ApplicationPlatform {

    actual fun initialize() {
        setCrashlyticsUnhandledExceptionHook()
        NotifierManager.initialize(NotificationPlatformConfiguration.Ios(showPushNotification = true))
    }
}
