package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class ChatsViewModel(
    private val messagesRepository: MessagesRepository,
) : BaseViewModel<ChatsState>(ChatsState()) {

    init {
        loadData()
    }

    private fun loadData() {
        launch("chats_load") {
            runCatching { messagesRepository.getChats() }
                .onFailure {
                    proceedError(it)
                    // todo: update state
                }
                .onSuccess { chats ->
                    mutableState.update {
                        it.copy(
                            chats = chats,
                        )
                    }
                }
        }
    }
}
