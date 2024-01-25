package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.data.db.entity.LocalContact
import io.github.mklkj.kommunicator.data.repository.ContactRepository
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.uuid.UUID
import org.koin.core.annotation.Factory

@Factory
class ContactsViewModel(
    private val userRepository: UserRepository,
    private val contactRepository: ContactRepository,
    private val messagesRepository: MessagesRepository,
) : BaseViewModel<ContactsState>(ContactsState()) {

    init {
        loadData()
    }

    private fun loadData() {
        launch("load_contacts", cancelExisting = false, isFlowObserver = true) {
            val userId = userRepository.getCurrentUser().id
            runCatching { contactRepository.refreshContacts() }
                .onFailure { proceedError(it) }

            contactRepository.getContacts(userId)
                .catch { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            errorMessage = error.message,
                            isLoading = false,
                        )
                    }
                }
                .onEach { contacts ->
                    mutableState.update {
                        it.copy(
                            errorMessage = null,
                            isLoading = false,
                            contacts = contacts,
                        )
                    }
                }
                .collect()
        }
    }

    fun onCreateChat(contact: LocalContact) {
        val chatId = UUID()
        launch("create_chat_${contact.id}", cancelExisting = false) {
            mutableState.update { it.copy(isLoading = true) }
            runCatching { messagesRepository.createChat(chatId, listOf(contact)) }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(errorMessage = error.message)
                    }
                }
                .onSuccess {
                    mutableState.update {
                        it.copy(createdChat = chatId)
                    }
                }
            mutableState.update { it.copy(isLoading = false) }
        }
    }

    fun onConversationOpened() {
        mutableState.update {
            it.copy(createdChat = null)
        }
    }
}
