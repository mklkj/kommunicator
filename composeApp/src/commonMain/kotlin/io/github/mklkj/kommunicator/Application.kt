package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import co.touchlab.kermit.platformLogWriter
import com.mmk.kmpnotifier.notification.NotifierManager
import io.github.mklkj.kommunicator.data.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Application {

    @OptIn(ExperimentalKermitApi::class, DelicateCoroutinesApi::class)
    fun initialize() {
        ApplicationPlatform.initialize()
        enableCrashlytics()
        Logger.setLogWriters(buildList {
            if (BuildKonfig.IS_DEBUG) {
                add(platformLogWriter())
            } else add(CrashlyticsLogWriter())
        })

        NotifierManager.addListener(object : NotifierManager.Listener, KoinComponent {
            val userRepository by inject<UserRepository>()

            override fun onNewToken(token: String) {
                GlobalScope.launch(CoroutineExceptionHandler { _, throwable ->
                    Logger.e("NotifierManager", throwable)
                }) {
                    userRepository.sendPushToken(token)
                }
            }
        })
    }
}

expect object ApplicationPlatform {
    fun initialize()
}
