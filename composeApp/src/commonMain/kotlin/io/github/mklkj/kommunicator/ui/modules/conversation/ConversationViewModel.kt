package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.uuid.UUID
import org.koin.core.annotation.Factory

@Factory
class ConversationViewModel(
    private val messagesRepository: MessagesRepository,
) : BaseViewModel<ConversationState>(ConversationState()) {

    fun loadData(chatId: UUID) {
        launch("chat_load_$chatId", cancelExisting = false) {
            runCatching { messagesRepository.getChatDetails(chatId) }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .onSuccess { details ->
                    mutableState.update {
                        it.copy(
                            details = details,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun sendMessage(chatId: UUID, content: String) {
        launch("chat_send_message") {
            mutableState.update { it.copy(isLoading = true) }
            messagesRepository.sendMessage(chatId, content)
            mutableState.update { it.copy(isLoading = false) }
        }
    }
}
