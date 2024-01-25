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
            runCatching {
                val user = userRepository.getCurrentUser()
                val chats = messagesRepository.getChats()
                Pair(user, chats)
            }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            error = error,
                            isLoggedIn = error !is UserTokenExpiredException,
                            isLoading = false,
                        )
                    }
                }
                .onSuccess { (user, chats) ->
                    mutableState.update {
                        it.copy(
                            chats = chats,
                            isLoading = false,
                            error = null,
                            userAvatarUrl = user.avatarUrl,
                        )
                    }
                }
        }
    }

    fun onRefresh() {
        mutableState.update {
            it.copy(isLoading = true)
        }
        loadData()
    }
}
