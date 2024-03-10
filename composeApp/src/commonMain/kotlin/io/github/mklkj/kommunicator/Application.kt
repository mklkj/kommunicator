package io.github.mklkj.kommunicator

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import co.touchlab.kermit.platformLogWriter
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.PayloadData
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.data.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.uuid.toUUID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Application {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e("NotifierManager", throwable)
    }

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
            val messageRepository by inject<MessagesRepository>()
            val json by inject<Json>()

            override fun onNewToken(token: String) {
                GlobalScope.launch(exceptionHandler) {
                    userRepository.sendPushToken(token)
                }
            }

            override fun onPayloadData(data: PayloadData) {
                super.onPayloadData(data)

                val chatId = data["chat_id"]?.toString()
                    ?: return Logger.i("Push doesn't contain chat id property")
                val broadcastPayload = data["broadcast"]?.toString()
                    ?: return Logger.i("Push doesn't contain broadcast property")

                GlobalScope.launch(exceptionHandler) {
                    val broadcast = json.decodeFromString<MessageBroadcast>(broadcastPayload)
                    messageRepository.handleReceivedMessage(chatId.toUUID(), broadcast)
                }
            }
        })
    }
}

expect object ApplicationPlatform {
    fun initialize()
}
