package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.exceptions.UserTokenExpiredException
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class ChatsViewModel(
    private val userRepository: UserRepository,
    private val messagesRepository: MessagesRepository,
) : BaseViewModel<ChatsState>(ChatsState()) {

    init {
        loadData()
        checkCurrentUser()
        onRefresh()
    }

    private fun checkCurrentUser() {
        launch("check_current_user") {
            runCatching { userRepository.getCurrentUser() }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(isLoggedIn = error !is UserTokenExpiredException)
                    }
                }
                .onSuccess { user ->
                    mutableState.update {
                        it.copy(userAvatarUrl = user.avatarUrl)
                    }
                }
        }
    }

    private fun loadData() {
        launch("chats_load") {
            messagesRepository.observeChats()
                .catch { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            error = error,
                            isLoading = false,
                        )
                    }
                }
                .onEach { chats ->
                    mutableState.update {
                        it.copy(
                            chats = chats,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                .collect()
        }
    }

    fun onRefresh() {
        launch("refresh_chats", cancelExisting = false) {
            mutableState.update { it.copy(isLoading = true) }
            runCatching { messagesRepository.refreshChats() }
                .onFailure { error ->
                    mutableState.update {
                        it.copy(error = error)
                    }
                }
            mutableState.update { it.copy(isLoading = false) }
        }
    }
}
