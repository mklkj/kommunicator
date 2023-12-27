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
                .onFailure {
                    proceedError(it)
                }
                .onSuccess { details ->
                    mutableState.update {
                        it.copy(details = details)
                    }
                }
        }
    }
}
