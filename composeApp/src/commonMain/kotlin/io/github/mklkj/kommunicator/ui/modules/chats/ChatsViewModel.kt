package io.github.mklkj.kommunicator.ui.modules.chats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatsViewModel(
    private val messagesRepository: MessagesRepository,
) : BaseViewModel() {

    var chats by mutableStateOf<List<Chat>>(emptyList())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            runCatching { messagesRepository.getChats() }
                .onFailure { it.printStackTrace() }
                .onSuccess {
                    chats = it
                }
        }
    }
}
