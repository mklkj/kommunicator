package io.github.mklkj.kommunicator.data.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton


@Singleton
class NotificationService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val json: Json,
) {

    init {
        FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
        )
    }

    suspend fun notifyParticipants(
        chatId: UUID,
        messageId: UUID,
        alreadyNotifiedUsers: List<UUID>,
        broadcast: MessageBroadcast,
    ) {
        val chatParticipants = chatRepository.getChatParticipantsPushTokens(chatId)

        val registrationTokens = chatParticipants
            .filterNot { it.userId in alreadyNotifiedUsers }
            .map { it.token }
        if (registrationTokens.isEmpty()) {
            return println("There is no user to notify (lack of push tokens)")
        }

        val messageToNotify = messageRepository.getMessage(messageId)
        val message = MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(messageToNotify.author ?: messageToNotify.firstName)
                    .setBody(messageToNotify.content)
                    .build()
            )
            .putData("broadcast", json.encodeToString(broadcast))
            .putData("chat_id", chatId.toString())
            .addAllTokens(registrationTokens)
            .build()
        val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)
        println(response.successCount.toString() + " of ${registrationTokens.size} messages were sent successfully, ${response.failureCount} fail")

        if (response.failureCount > 0) {
            response.responses.forEach {
                if (!it.isSuccessful) {
                    it.exception.printStackTrace()
                }
            }
        }
    }
}
