package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatsViewModel(
    private val messagesRepository: MessagesRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ChatsState())
    val uiState: StateFlow<ChatsState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            runCatching { messagesRepository.getChats() }
                .onFailure { it.printStackTrace() }
                .onSuccess { chats ->
                    _uiState.update {
                        it.copy(
                            chats = chats,
                        )
                    }
                }
        }
    }
}
