package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.data.repository.ContactRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Singleton

@Singleton
class ContactsViewModel(
    private val contactRepository: ContactRepository,
) : BaseViewModel<ContactsState>(ContactsState()) {

    init {
        loadData()
    }

    private fun loadData() {
        launch("load_contacts", cancelExisting = false) {
            runCatching { contactRepository.getContacts()}
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            errorMessage = error.message,
                            isLoading = false,
                        )
                    }
                }
                .onSuccess { contacts ->
                    mutableState.update {
                        it.copy(
                            errorMessage = null,
                            isLoading = false,
                            contacts = contacts,
                        )
                    }
                }
        }
    }
}
