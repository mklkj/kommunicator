package io.github.mklkj.kommunicator.ui.modules.contacts.add

import io.github.mklkj.kommunicator.data.repository.ContactRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class ContactAddViewModel(
    private val contactRepository: ContactRepository,
) : BaseViewModel<ContactAddState>(ContactAddState()) {

    fun addContact(username: String) {
        launch("add_contact", cancelExisting = false) {
            mutableState.update {
                it.copy(isLoading = true)
            }
            runCatching { contactRepository.addContact(username) }
                .onFailure { error ->
                    mutableState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
                .onSuccess {
                    mutableState.update {
                        it.copy(isLoading = false, isSuccess = true)
                    }
                }
        }
    }
}
