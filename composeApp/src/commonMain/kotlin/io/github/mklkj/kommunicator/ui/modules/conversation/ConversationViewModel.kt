package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class ConversationViewModel(
    private val messagesRepository: MessagesRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(ConversationState())
    val state = _state.asStateFlow()

    fun loadData(chatId: String) {
        launch("chat_load_$chatId") {
            runCatching { messagesRepository.getChatDetails(chatId) }
                .onFailure { it.printStackTrace() }
                .onSuccess { details ->
                    _state.update {
                        it.copy(details = details)
                    }
                }
        }
    }
}
