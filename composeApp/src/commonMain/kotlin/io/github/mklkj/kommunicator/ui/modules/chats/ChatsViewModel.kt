package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.exceptions.UserTokenExpiredException
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class ChatsViewModel(
    private val userRepository: UserRepository,
    private val messagesRepository: MessagesRepository,
) : BaseViewModel<ChatsState>(ChatsState()) {

    init {
        loadData()
    }

    private fun loadData() {
        launch("chats_load") {
            runCatching { messagesRepository.getChats() }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            errorMessage = error.message,
                            isLoggedIn = error !is UserTokenExpiredException,
                            isLoading = false,
                        )
                    }
                }
                .onSuccess { chats ->
                    mutableState.update {
                        it.copy(
                            chats = chats,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun logout() {
        launch("logout_user", cancelExisting = false) {
            userRepository.logout()
        }
    }
}
