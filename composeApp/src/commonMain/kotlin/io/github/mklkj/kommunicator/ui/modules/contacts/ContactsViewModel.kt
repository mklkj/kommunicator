package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.data.repository.ContactRepository
import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Singleton

@Singleton
class ContactsViewModel(
    private val userRepository: UserRepository,
    private val contactRepository: ContactRepository,
) : BaseViewModel<ContactsState>(ContactsState()) {

    init {
        loadData()
    }

    fun loadData() {
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
}
