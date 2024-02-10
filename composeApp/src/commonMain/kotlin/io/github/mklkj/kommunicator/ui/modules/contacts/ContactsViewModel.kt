package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.Contacts
import io.github.mklkj.kommunicator.data.repository.ContactRepository
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class ContactsViewModel(
    private val contactRepository: ContactRepository,
    private val messagesRepository: MessagesRepository,
) : BaseViewModel<ContactsState>(ContactsState()) {

    init {
        loadData()
    }

    private fun loadData() {
        launch("load_contacts", cancelExisting = false, isFlowObserver = true) {
            contactRepository.observeContacts()
                .catch { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            error = error,
                            isLoading = false,
                        )
                    }
                }
                .onEach { contacts ->
                    mutableState.update {
                        it.copy(
                            error = null,
                            isLoading = false,
                            contacts = contacts,
                        )
                    }
                }
                .collect()
        }
    }

    fun onCreateChat(contact: Contacts) {
        launch("create_chat_${contact.id}", cancelExisting = false) {
            mutableState.update { it.copy(isLoading = true) }
            runCatching { messagesRepository.createChat(listOf(contact)) }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(error = error)
                    }
                }
                .onSuccess { chatId ->
                    mutableState.update {
                        it.copy(createdChat = chatId)
                    }
                }
            mutableState.update { it.copy(isLoading = false) }
        }
    }

    fun onRefresh() {
        launch("refresh_contacts", cancelExisting = false) {
            mutableState.update { it.copy(isLoading = true) }
            runCatching { contactRepository.refreshContacts() }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            error = error,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onConversationOpened() {
        mutableState.update {
            it.copy(createdChat = null)
        }
    }
}
