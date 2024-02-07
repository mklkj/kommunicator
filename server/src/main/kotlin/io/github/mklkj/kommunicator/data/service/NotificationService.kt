package io.github.mklkj.kommunicator.data.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton


@Singleton
class NotificationService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
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
        alreadyNotifiedUsers: List<UUID>
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
            .addAllTokens(registrationTokens)
            .build()
        val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)
        println(response.successCount.toString() + " messages were sent successfully")
    }
}
